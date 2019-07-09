import React, { Component } from 'react';
import apple from '../img/binary-tree/apple.png';
import applePlanet from '../img/planets/apple_planet.png';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/Arrows.scss';
import '../css/BinaryTreeGame.scss';
import GameIntroScreen from './GameIntroScreen';
import BTPreview from './BTPreview';
import Timer from './Timer';
import GameOverScreen from './GameOverScreen';
import { enterGame, INPUT, handleInput, onGameOver, flashArrow , gameOnBlur, flashBoxGreen, flashBoxRed, backButton } from './GameFunctions';
import StandingsDisplay from './StandingsDisplay';
import bt_instr_1 from '../img/binary-tree/bt_left_input.png';
import bt_instr_2 from '../img/binary-tree/bt_right_input.png';
import bt_instr_3 from '../img/binary-tree/bt_up_input.png';
import bt_instr_4 from '../img/binary-tree/bt_enter_input.png';

/**
 * BINARY TREE GAME:
 * The player searches though a binary search tree of apples to find as many targets as they can in the given time limit.
 *
 *  MULTIPLAYER COMPATABLE
 *
 * Expects the following props:
 *          @prop username : username of current player
 *          @prop onLeave : function called when player exits game
 *          @prop backToArena: function called when
 */
class BinaryTreeGame extends Component {

  constructor(props) {
    super(props);
    this.props = {
      onLeave: undefined, // how to leave this game and return to space
  };

    this.state = {
        targetVal: -1,
        currVal: -5,
        score: 0,
        time: 60,
        name: "Bin-apple Trees",
        startTime: 30,
        entered: false,
        gameOver: false,
        players: [],
        playerStateMap: {},
        multiplayer: false,
        highScore: 0,
        nextPoints:100, //the number of points you will get for getting the next thing right.
    };

    this.mainGame = React.createRef();
    this.timer = React.createRef();
    this.targetRef = React.createRef();

    //INSTRUCTIONS
    this.instructionsText = "Search through a tree of apples to find the target! " +
       "Each apple has a left child (with a lesser value) and a right child (greater value).";

    this.inputGraphics = [
      <img key={"bt-instr-1"} src={bt_instr_1} className="instructionsPic"/>,
      <img key={"bt-instr-2"} src={bt_instr_2} className="instructionsPic"/>,
      <img key={"bt-instr-3"} src={bt_instr_3} className="instructionsPic"/>,
      <img key={"bt-instr-4"} src={bt_instr_4} className="instructionsPic"/>];

    this.btPrev = React.createRef();
}

/**
 * Gets starting info from game
 *
 * @param response response package from backend
 * */
onStartResp(response){
   let parsedRoot = JSON.parse(response.payload.root);
   let parsedTarget = JSON.parse(response.payload.targetNode);
   let parsedPlayers = JSON.parse(response.payload.players);

   //reset all stored player states
   this.setState({playerStateMap: {}});
   parsedPlayers.forEach(username => this.state.playerStateMap[username] = {currNode: parsedRoot.value, score: 0});
   this.setState({}); //rerender

     this.setState({
        currVal: parsedRoot.value,
        targetVal: parsedTarget.value,
        players: parsedPlayers,
        startTime: response.payload.maxTime,
        nextPoints:100
   })

   setTimeout(() => {
     if(this.btPrev.current) this.btPrev.current.bindPlayers(parsedPlayers)}, 200);
}

/**
 * When a user makes a move, backend will call with results.
 *
 * @param repsonse response package from backend
 * */
onActionResp(response){
  //unpack what the backend sent
  let payload = response.payload;
  let parsedTarget =  JSON.parse(payload.targetNode);
  let parsedPlayers = JSON.parse(payload.players);

  //updates from backend
  this.setState({
    targetVal: parsedTarget.value,
    score: parsedPlayers[this.props.username]["score"]
  });

   //for each player, update score
  Object.keys(parsedPlayers).forEach((username) => {
    this.state.playerStateMap[username]["score"] = parsedPlayers[username]["score"];
  });

    //only update current player info if it was this player that moved
    if(this.props.username === payload.userWhoMoved) {
      this.updateForCurrentPlayer(parsedPlayers, response);
    } else {
      //else update preview to reflect other players' movement
        switch(parsedPlayers[payload.userWhoMoved]["lastClicked"]){
            case INPUT.LEFT:
                this.btPrev.current.changeActive("left", payload.userWhoMoved);
                break;
            case INPUT.RIGHT:
                this.btPrev.current.changeActive("right", payload.userWhoMoved);
                break;
            case INPUT.UP:
                this.btPrev.current.changeActive("parent", payload.userWhoMoved);
                break;
            case INPUT.ENTER:
                if(response.payload.valid) {
                    //if someone else found the target, flash the target box red
                    flashBoxRed(this.targetRef);
                }
                break;
            default:
                break;
        }
    }

    this.setState({}); //update

  }

  /**
   * Update graphics for when it is THIS player who moved. Starts animations.
   *
   * @param parsedPlayers : map of players to info dictionaries ({currNode, lastClicked, score})
   * @param response : response package from backend
  */
  updateForCurrentPlayer(parsedPlayers, response){
        const input = parsedPlayers[this.props.username]["lastClicked"];
        const parsedNode = JSON.parse(parsedPlayers[this.props.username]["currNode"]);
        const valid = response.payload.valid;

        //their score goes down if they move the wrong direction
        let prevNextScore = this.state.nextPoints;
        let nextScoreVal =  parsedPlayers[this.props.username]["nextScoreVal"];
            this.setState({
              nextPoints: nextScoreVal
            });
          //if score went down, flash score red by setting missedGuess to true
            if(prevNextScore > nextScoreVal){
              this.setState({
                missedGuess:true
              });
              setTimeout(() => {
                this.setState({
                  missedGuess:false
                });
            }, 400);
            }

      //if enter was pressed, flash target box according to correctness
      if(input == INPUT.ENTER){
        if(valid){
            flashBoxGreen(this.targetRef);
        } else {
            flashBoxRed(this.targetRef);
        }
      } else {  //else left/right/up was pressed

          if(valid){
            //change apple
            this.changeApple(parsedNode.value);

            //animation for certain arrow according to input
            if(input == INPUT.LEFT) {flashArrow(this.refs.leftArrow, "grow-left-arrow");}
            if(input == INPUT.RIGHT) {flashArrow(this.refs.rightArrow, "grow-right-arrow");}
            if(input == INPUT.UP) {flashArrow(this.refs.upArrow, "grow-up-arrow");}

          } else {
            //animation for certain arrow according to input
            if(input == INPUT.LEFT) {flashArrow(this.refs.leftArrow, "grow-left-arrow-red");}
            if(input == INPUT.RIGHT) {flashArrow(this.refs.rightArrow, "grow-right-arrow-red");}
            if(input == INPUT.UP) {flashArrow(this.refs.upArrow, "grow-up-arrow-red");}
          }
        }
  }

/**
 * Fades apple in with passed-in value
 *
 * @num number for new cookie
 * */
changeApple(num){
   //fade apple out
   let appleRef = this.refs.currApple;
   appleRef.classList.add("fadeOut");

   //change num
   this.setState({
    currVal: num,
   })

   //in 400 ms, fade in
   setTimeout( () =>  {
     appleRef.classList.remove("fadeOut");

     //change number
     appleRef.classList.add("fadeIn");

     //in 500 ms, remove fadeIn class
     setTimeout( function () {
       appleRef.classList.remove("fadeIn")
     }, 200);
   }, 100);
}

//fixes focus issue
componentDidMount() {
  document.getElementById("main-game-div").focus();
}

/**
 * KEY EVENTS
 *    @left key: move to left child
 *    @right key: move to right child
 *    @up key: move up to parent
 *    @enter @space key: found the target node
 *    @escape : end game
 */
onKeyDown = (event) => {
  //if game isn't playing, do nothing
  if(this.state.gameOver || !this.state.entered){
    return;
  }


  switch(event.keyCode){
    case 37: case 65: //left
        handleInput(INPUT.LEFT, this.props.username);
        this.btPrev.current.changeActive("left", this.props.username);
       break;
    case 39: case 68: //right
       handleInput(INPUT.RIGHT, this.props.username);
       this.btPrev.current.changeActive("right", this.props.username);
       break;
    case 38: case 87: //up or w
       handleInput(INPUT.UP, this.props.username);
       this.btPrev.current.changeActive("parent", this.props.username);
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

  render() {
    let content;
    //IF GAME IS NOT YET ENTERED - SHOW INTRO SCREEN
    if(!this.state.entered){
      content = <GameIntroScreen title={this.state.name} instructions={this.instructionsText}
                                submit={() => enterGame(this)} dataStructure="Binary Tree"
                                inputGraphics={this.inputGraphics} planetUrl={applePlanet} topOffset={-75} leftOffset={0} width={205}
                                back={this.props.onLeave} showButtons={!this.state.multiplayer}/>
    } else {
        //IF GAME IS BEING PLAYED
      if(!this.state.gameOver){

        content = <div className={"game-div"}>
         {backButton(this)}
        {/* /* target window */}
          <div className="target-div" ref={this.targetRef}>
              <h2 className="target-label" ref={this.mainGame} style={{marginBottom:"5px"}}>TARGET</h2>
              <div className="target" style={ { backgroundImage:`url(${apple})`, height: "135px", paddingTop: "15px" }}> {this.state.targetVal} </div>
              {/* popup for missing score or just a reminder of point value you will earn next.*/}
              <div className={"next-points-div " + (this.state.missedGuess ? "missed-points-div" : "")}>
                REWARD
              <h1>{this.state.nextPoints}</h1>
              </div>
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
                  style={ { backgroundImage:`url(${apple})`, height: "305px", paddingTop: "45px" }}
                  onKeyPress={this.handleKeyPress}
                  ref="currApple" >{this.state.currVal}</div>

            {/* arrows */}
            <i className="arrow right grow down-shift" ref="rightArrow"></i>
            <div className="right-child-label arrow-label">RIGHT CHILD</div>

            <i className="arrow left grow down-shift" ref="leftArrow"></i>
            <div className="left-child-label arrow-label">LEFT CHILD</div>

            <i className="arrow up grow" ref="upArrow"></i>
            <div className="parent-label arrow-label">PARENT</div>

            {/* Preview */}
            <BTPreview ref={this.btPrev} username={this.props.username}/>

            </div>

            document.getElementById("main-game-div").focus();  //fixes focus issues
    } else {
        //IF GAME IS OVER
        content = <GameOverScreen title={this.state.name}
        replay={() => enterGame(this)} back={this.props.onLeave} dataStructure="Binary Tree"
        planetUrl={applePlanet} topOffset={-75} leftOffset={0} score={this.state.score}
        multiplayer={this.state.players.length > 1} backToArena={() => this.props.backToArena()}
        username={this.props.username} players={this.state.playerStateMap} highScore={this.state.highScore}/>
    }
    }

    return (
      <div tabIndex="0"  id="main-game-div" className="game-div"  onBlur={() => gameOnBlur(this)} onKeyDown={this.onKeyDown} >
          {content}
      </div>
    );
  }
}

export default BinaryTreeGame;
