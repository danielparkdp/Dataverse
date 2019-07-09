import React, { Component } from 'react';
import cookie from '../img/linked-list/cookie.png';
import cookiePlanet from '../img/planets/cookie_planet.png';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/LinkedListGame.scss';
import '../css/Arrows.scss';
import GameIntroScreen from './GameIntroScreen';
import LLPreview from './LLPreview';
import Timer from './Timer';
import GameOverScreen from './GameOverScreen';
import leftKeyPic from '../img/linked-list/ll_left_arrow.png';
import {socket, MESSAGE_TYPE} from "../App";
import {INPUT, enterGame, handleInput, onGameOver, flashArrow, gameOnBlur, flashBoxRed , flashBoxGreen, backButton} from './GameFunctions';
import StandingsDisplay from './StandingsDisplay';
import ll_instr_1 from '../img/linked-list/ll_left_arrow.png';
import ll_instr_2 from '../img/linked-list/ll_right_arrow.png';
import ll_instr_3 from '../img/linked-list/ll_enter.png';

/**
 * LINKED LIST GAME:
 * The player searches though a linked list of cookies to find as many targets as they can in the given time limit.
 *  
 *  MULTIPLAYER COMPATABLE
 * 
 * Expects the following props:
 *          @prop username : username of current player
 *          @prop onLeave : function called when player exits game
 *          @prop backToArena: function called when 
 */
class LinkedListGame extends Component {

  constructor(props) {
    super(props);

    this.state = {
        name: "Choco Chip Links",
        targetVal: 1,
        currVal: 5,
        score: 0,
        startTime: 30,
        entered: false,
        gameOver: false,
        players: [],
        playerStateMap: {},
        multiplayer: false,
        highScore: 0,
        nextPoints:50, //the number of points you will get for getting the next thing right.
    };

    this.mainGame = React.createRef();
    this.llPrev = React.createRef();
    this.timer = React.createRef();
    this.targetBoxRef = React.createRef();

    this.instructionsText = "Search through a Linked List of cookies to find as many targets " +
       "as you can in 30 seconds!";

    //list of instruction graphics to be passed in to intro screen
    this.inputGraphics = [
      <img key={"ll-instr-1"} src={ll_instr_1} className="instructionsPic"/>,
      <img key={"ll-instr-2"} src={ll_instr_2} className ="instructionsPic"/>,
      <img key={"ll-instr-3"} src={ll_instr_3} className="instructionsPic"/>];
}

componentDidMount() {
     document.getElementById("main-game-div").focus();
}

/**
 * KEY EVENTS
 *    left key: move to previous node in list
 *    right key: move to next node in list
 *    enter/space key: found the target node
 *    esc: end game
 */
onKeyDown = (event) => {
    //if game isn't playing, do nothing
    if(this.state.gameOver || !this.state.entered){
      return;
    }

  switch(event.keyCode){
    case 37: case 65: //left
        this.llPrev.current.changeActive("prev", this.props.username);
        handleInput(INPUT.LEFT, this.props.username);
       break;
       case 39: case 68: //right
       this.llPrev.current.changeActive("next", this.props.username);
       handleInput(INPUT.RIGHT, this.props.username);
       break;
    case 13: case 32: //enter or space
        handleInput(INPUT.ENTER, this.props.username);
       break;
    case 27: //esc
       onGameOver(this);
      break;
    default:
       break;
  }
}

/**  
 * Gets starting info from game (players, initial target, start node)
 *   @param response : backend response
 */
onStartResp(response){
  let parsedNode = JSON.parse(response.payload.startNode);
  let parsedTarget = JSON.parse(response.payload.targetNode);
  let parsedPlayers = JSON.parse(response.payload.players);

   //reset all stored player states
   this.setState({playerStateMap: {}});
   parsedPlayers.forEach(username => this.state.playerStateMap[username] = {currNode: parsedNode.value, score: 0});
   this.setState({}); //rerender

    this.setState({
     currVal: parsedNode,
     targetVal: parsedTarget,
     players: parsedPlayers,
  });

  setTimeout(() => { if(this.llPrev.current) this.llPrev.current.bindPlayers(parsedPlayers)}, 200);
}



/**
 * After user makes a move, start animations that indicate whether the move
 *  was valid, as well as update score if necessary 
 *  
 * @param response: response from backend
*/
onActionResp(response){
  //unpack what the backend sent
  let payload = response.payload;
  let parsedTarget =  payload.target;
  let parsedPlayers = JSON.parse(payload.players);
  let currPlayerStats = parsedPlayers[this.props.username];
  let prevScore = this.state.playerStateMap[payload.userWhoMoved]["score"];

  //any updates from backend
   this.setState({
      targetVal: parsedTarget,
      score: currPlayerStats["score"],
   })

     //for each player, update score
  Object.keys(parsedPlayers).forEach((username) => {
    this.state.playerStateMap[username]["score"] = parsedPlayers[username]["score"];
  });

      //only update current player info if it was this player that moved
      if(this.props.username === payload.userWhoMoved){
           this.updateForCurrentPlayer(currPlayerStats, prevScore < this.state.score); 
      } else {
        //update linked list preview because someone else moved
        let input = parsedPlayers[payload.userWhoMoved]["lastClicked"];
        if(input == INPUT.LEFT){
            this.llPrev.current.changeActive("prev", payload.userWhoMoved);
        } else if (input == INPUT.RIGHT){
            this.llPrev.current.changeActive("next", payload.userWhoMoved);
        } else if (input == INPUT.ENTER) {
           //if someone else found the target, flash the target box red
          let newScore = this.state.playerStateMap[payload.userWhoMoved]["score"];
          if(newScore > prevScore) flashBoxRed(this.targetBoxRef);
        }
      }

  this.setState({}); //rerender
  }

  /**
   * updates the graphics when the current user makes a move (called from above method)
   * 
   * @param currPlayer current player dictionary {"score", "lastClicked"}
   * @param validMoce bool for whether move is valid
   * 
   * */
  updateForCurrentPlayer(currPlayer, validMove){
    let input = currPlayer["lastClicked"];
    let nextScoreVal = currPlayer["nextScoreVal"]
    this.setState({
      nextPoints:nextScoreVal
    });

    //animation for changing the cookie
   if(input == INPUT.LEFT || input == INPUT.RIGHT){
    this.changeCookie(currPlayer["currNode"]);

    //animation for certain arrow according to input
    if(input == INPUT.LEFT) {flashArrow(this.refs.leftArrow, "grow-left-arrow");}
    if(input == INPUT.RIGHT) {flashArrow(this.refs.rightArrow, "grow-right-arrow");}

    // else enter was pressed - flash target box red or green indicating correctness
    } else if(input == INPUT.ENTER){
        //if valid, flash green
        if(validMove) {
            flashBoxGreen(this.targetBoxRef);
        } else {
            flashBoxRed(this.targetBoxRef);
        }
      }
  }

  /** 
   * Fades cookie in with passed-in value
   * @param num value of new cookie
   *  */
changeCookie(num){
  //fade apple out
  let cookieRef = this.refs.currCookie;
  cookieRef.classList.add("fadeOut");

  //change num
  this.setState({
      currVal: num,
  })

  //in 400 ms, fade in
  setTimeout( () =>  {
    cookieRef.classList.remove("fadeOut");

    //change number
    cookieRef.classList.add("fadeIn");

    //in 500 ms, remove fadeIn class
    setTimeout( function () {
      cookieRef.classList.remove("fadeIn")
    }, 200);
  }, 100);
}

    /* called when user decides to exit game */
    onLeave= () => {
      this.props.onLeave();
    };

  render() {
    //show intro screen if game has yet to be entered
    let content;
    if(!this.state.entered){
      content = <GameIntroScreen title={this.state.name} instructions={this.instructionsText}
                                submit={() => enterGame(this)} dataStructure="(Doubly) Linked List"
                                inputMap={this.inputMap} planetUrl={cookiePlanet} topOffset={-40} 
                                leftOffset={-20} inputGraphics={this.inputGraphics} back={this.props.onLeave}
                                showButtons={!this.state.multiplayer} />
    } else {
      //IF GAME IS BEING PLAYED
      if(!this.state.gameOver){

        //the points you will receive next (if u guess correctly)
        let nextPoints = <div className="next-points-div" style={{paddingTop:"15px"}}>
          REWARD
          <h1>{this.state.nextPoints}</h1>
        </div>

        content = <div className="game-content-div">
          {backButton(this)}

        {/* /* target window */}
          <div className="target-div" ref={this.targetBoxRef}>
              <h2 className="target-label" ref={this.mainGame} >TARGET</h2>
              <div className="target" style={ { backgroundImage:`url(${cookie})`, width: "140px", height: "140px" }}> {this.state.targetVal} </div>
              {nextPoints}
          </div>

          {/* score and time*/}
          <div className="score-box">
                SCORE
              <h2 style={{fontSize: "32px"}}>{this.state.score}</h2>
          </div>

           {/* Show other players' scores if this is multiplayer */}
        {this.state.playerStateMap && this.state.players.length > 1 ? <StandingsDisplay players={this.state.playerStateMap}/> : undefined }


          <Timer ref={this.timer} startTime={this.state.startTime} onEnd={() => onGameOver(this)} />

          {/* current node */}
            <div className="curr-node"
                  style={ { backgroundImage:`url(${cookie})`}}
                  onKeyPress={this.handleKeyPress}
                  ref="currCookie" >{this.state.currVal}</div>

            {/* arrows */}
            <i className="arrow right grow" ref="rightArrow"></i>
            <div className="prev-label arrow-label">PREVIOUS</div>

            <i className="arrow left grow" ref="leftArrow"></i>
            <div className="next-label arrow-label">NEXT</div>

            {/* PREVIEW */}
            <LLPreview ref={this.llPrev} username={this.props.username}/>

            </div>

          document.getElementById("main-game-div").focus();
        } else {
          //IF GAME IS OVER
          content = <GameOverScreen title={this.state.name}
                  replay={() => enterGame(this)} back={this.props.onLeave} dataStructure="(Doubly) Linked List"
                  planetUrl={cookiePlanet} topOffset={-40} leftOffset={-20} score={this.state.score}
                  multiplayer={this.state.players.length > 1} backToArena={() => this.props.backToArena()} 
                  username={this.props.username} players={this.state.playerStateMap} highScore={this.state.highScore}/>
        }

    }

    return (
      <div tabIndex="0"  id="main-game-div" className="game-div"  onKeyDown={this.onKeyDown} onBlur={() => gameOnBlur(this)}>
          {content}
      </div>
    );
  }
}

export default LinkedListGame;
