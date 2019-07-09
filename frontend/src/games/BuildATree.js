import React, { Component } from 'react';
import '../css/App.scss';
import '../css/Game.scss';
import orange from '../img/build-tree/orange.png';
import orangePlanet from '../img/planets/orange_planet.png';
import GameIntroScreen from './GameIntroScreen';
import Timer from './Timer';
import GameOverScreen from './GameOverScreen';
import { enterGame, INPUT, sendAction, onGameOver, gameOnBlur, flashBoxGreen, flashBoxRed, backButton } from './GameFunctions';
import BinaryTreeRepresentation from './BinaryTreeRepresentation'
import '../css/BinaryTreeRepresentation.scss';
import bt_instr from '../img/build-tree/build-tree-instructions.png';
import build_instr_1 from '../img/build-tree/build-tree-instr-1.png';
import build_instr_2 from '../img/build-tree/build-tree-instr-2.png';
import build_instr_3 from '../img/build-tree/build-tree-instr-3.png';
import build_instr_4 from '../img/build-tree/build-tree-instr-4.png';

/**
 * BUILD A TREE:
 * The player grows an orange tree by placing values in the tree according to the rules of a BST.
 *
 * Expects the following props:
 *          @prop username : username of current player
 *          @prop onLeave : function called when player exits game
 *          @prop backToArena: function called when
 */
class BuildATree extends Component{

    constructor(props) {
        super(props);

      this.state = {
        currVal: -5,
        score: 0,
        name: "Build A Tree",
        startTime: 60,
        entered: false,
        gameOver: false,
        numPlayers: 1, //default
        nodeValues:{},
        missedGuess:false, //whether a recent guess was wrong.
        nextPoints:50, //the number of points you will get for getting the next thing right.
    };

    this.mainGame = React.createRef();
    this.timer = React.createRef();
    this.targetRef = React.createRef();

    this.instructionsText = "Grow your very own orange tree! (in space?!?!)";

    this.inputGraphics = [
      <img key={"build-instr-1"} src={build_instr_1} className="instructionsPic"/>,
      <img key={"build-instr-2"} src={build_instr_2} className="instructionsPic"/>,
      <img key={"build-instr-3"} src={build_instr_3} className="instructionsPic"/>,
      <img key={"build-instr-4"} src={build_instr_4} className="instructionsPic"/> ];

    }

    componentDidMount() {
      document.getElementById("main-game-div").focus();
    }

    //gets starting info from game
    onStartResp(response){
       let rootVal = JSON.parse(response.payload.rootValue);
       let currVal = JSON.parse(response.payload.currValue);
       let prevNodeVals = this.state.nodeValues
       prevNodeVals["b"]=rootVal
        prevNodeVals["bl"]=null
        prevNodeVals["br"]=null
         this.setState({
            currVal: currVal,
            nodeValues:prevNodeVals,
            startTime: response.payload.maxTime
       });
    }


    onActionResp(response){
      let payload = response.payload
      //unpack what the backend sent
      let valid = payload.valid
      //update state and flash green
      if(valid){
        let currValue =  JSON.parse(payload.currValue);
        let newNode = JSON.parse(payload.newNodeToDisp);
        let score = JSON.parse(payload.score)
        let prevNodeVals = this.state.nodeValues
        prevNodeVals[newNode.id] = newNode.value
        //make 2 "clickable nodes" by assigning a vlue of null to left and right child
        if(newNode.id.length<5){
        prevNodeVals[newNode.id+"l"]=null
        prevNodeVals[newNode.id+"r"]=null
      }
      this.setState({
        score:score,
        currVal: currValue,
        nodeValues:prevNodeVals,
        nextPoints:50
      });
      flashBoxGreen(this.targetRef);
      }else{
        //get the number of points they will get for next correct guess
        let nextScoreVal =  JSON.parse(payload.nextScoreVal);
        //flash target and clicked node red
        flashBoxRed(this.targetRef);
        this.setState({
          missedGuess:true,
          nextPoints:nextScoreVal
        });
        //wait 1 second then set it back to false
        setTimeout(() => {
          this.setState({
            missedGuess:false
          });
      }, 300);
      }
    }


/**
 * Resets game when start or replay button is pressed.
 */
startGameBtnPressed() {
  //reset all the order/build/serve
  this.setState({
    nodeValues:{}
  });
  enterGame(this);   //enter the game!
}


validateClick=(event)=>{
  const payload = {
    id: event.currentTarget.id,
    username: this.props.username
  };
  sendAction(payload);
}

//escape the game if user presses escape
onKeyDown = (event)  => {
    //if game isn't playing, do nothing
    if(this.state.gameOver || !this.state.entered){
      return;
    }

  if(event.keyCode == 27){
    onGameOver(this);
  }
}


render() {
  //show intro screen if game has yet to be entered
  let content;
  if(!this.state.entered){
    content = <GameIntroScreen title={this.state.name} instructions={this.instructionsText}
                              submit={() => this.startGameBtnPressed()} dataStructure="Binary Tree"
                              inputGraphics={this.inputGraphics} planetUrl={orangePlanet} topOffset={-80} leftOffset={-20}
                              back={this.props.onLeave}/>
  } else {
      //IF GAME IS BEING PLAYED
    if(!this.state.gameOver){

      content = <div className={"game-div"}>

      {backButton(this)}

      {/* /* target window */}
        <div className="target-div" ref={this.targetRef}>
            <h2 className="target-label" ref={this.mainGame} >ADD TO TREE</h2>
            <div className="target" style={ { backgroundImage:`url(${orange})`, height: "135px", width: "140px", paddingTop: "0px" }}> {this.state.currVal} </div>
            {/* popup for missing score or just a reminder of point value you will earn next.*/}
            <div className={"next-points-div"}>
                REWARD
              <h1>{this.state.nextPoints}</h1>
            </div>
        </div>


          {/* score and time*/}
        <div className="score-box">
              SCORE
            <h2 style={{fontSize: "32px"}}>{this.state.score}</h2>
        </div>

      <Timer ref={this.timer} startTime={this.state.startTime} onEnd={() => onGameOver(this)} />

      <BinaryTreeRepresentation nodes = {this.state.nodeValues} validateClick={this.validateClick} />

      </div>

          document.getElementById("main-game-div").focus();  //fixes focus issues
  } else {
      //IF GAME IS OVER
      content = <GameOverScreen title={this.state.name}
      replay={() => this.startGameBtnPressed()} back={this.props.onLeave} dataStructure="Binary Search Tree"
      planetUrl={orangePlanet} topOffset={-80} leftOffset={-30} score={this.state.score} scoreUnits="oranges added to tree"
      multiplayer={false} username={this.props.username} players={this.state.playerStateMap} highScore={this.state.highScore}/>
  }
  }

  return (
    <div tabIndex="0"  id="main-game-div" className="game-div"  onKeyDown={this.onKeyDown} onBlur={() => gameOnBlur(this)}>
        {content}
    </div>
  );
}


}


export default BuildATree
