import React, { Component } from 'react';
import burgerPlanet from '../img/planets/hamburger_planet.png';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/QueueGame.scss';
import GameIntroScreen from './GameIntroScreen';
import GameOverScreen from './GameOverScreen';
import Timer from './Timer';
import { enterGame, backButton, onGameOver, gameOnBlur, sendAction, flashBoxGreen, flashBoxRed  } from './GameFunctions';
import q_instr_1 from '../img/burger/stack_instructions2.png';
import q_instr_2 from '../img/burger/queue_instructions2.png';

/**
 * STACK/QUEUE GAME:
 * The player builds burgers to match incoming orders using stacks and queues.
 * 
 * Expects the following props:
 *          @prop username : username of current player
 *          @prop onLeave : function called when player exits game
 *          @prop backToArena: function called when 
 */
class QueueGame extends Component {

  ingredientMap;
  PLANET_LEFT_OFFSET = -30;
  PLANET_TOP_OFFSET = -50;
  DISPLAY_NAME = "Barbe-Queue Rush";

  //maps enum to classname
  ingName = {
      TOP: "burgTop",
      TOMATO:  "tomato",
      LETTUCE: "lettuce",
      CHEESE: "cheese",
      MEAT: "meat",
      BTM: "burgBtm",
      nextPoints:100, //the number of points you will get for getting the next thing right.
  };

  constructor(props) {
    super(props);

    this.state = {
        score: 0,
        name: "Barbe-Queue Rush",
        startTime: 60,
        entered: false,
        gameOver: false,
        order: [], 
        build: [], //what the user is building
        serve: [],
        mode: "None", 
        errorMsg:"",
        username: "",
        highScore: 0
    };

    //make refs
    this.mainGame = React.createRef();
    this.timer = React.createRef();
    this.planetImg = burgerPlanet;
    this.targetRef = React.createRef();

    this.instructionsText = "Use Stacks and Queues to make burgers for customers' orders.";

    //graphics for instructions 
    this.inputGraphics = [<img key={"q-instr-1"} src={q_instr_1} className="instructionsPic burger-instr"/>,
                         <img key={"q-instr-2"} src={q_instr_2} className="instructionsPic burger-instr"/>];

    this.ingredientList = [this.ingName.TOP, this.ingName.TOMATO, this.ingName.LETTUCE, this.ingName.CHEESE, this.ingName.MEAT, this.ingName.BTM];
    this.mappedIngs = [];

    this.nextId = 0;
}

//focus on mount
componentDidMount() {
  document.getElementById("main-game-div").focus();
}

/**
 * Resets game when start or replay button is pressed.
 */
startGameBtnPressed() {
  //reset all the order/build/serve
  this.setState({
    order: [], 
    build: [],
    serve: [],
  });
  enterGame(this);   //enter the game!
}

/* KEY EVENTS */
onKeyDown = (event) => {
  //if game isn't playing, do nothing
  if(this.state.gameOver || !this.state.entered){
    return;
  }
  switch(event.keyCode){
    case 13: case 32: //enter or space 
       this.serveBurger(); //serve the burger
       break;
    case 27: //esc
       onGameOver(this); //end game
       break;
    default:
       break;
  }
}

/**
 * Creates an ingredient dictionary with the given name, and clickable set to false.
 * @param {*} name : name of ingredient. 
 */
makeNewIngredient(name){
  let newId = this.nextId++;
  return {name: name, id: newId, clickable: false};
}

/**
 * Adds ingredient to build stack. Called when ingredient is clicked.
 * 
 * @param pointer event from clicking on ingredient
 */
addIngredient = (event) => {
    //if game isn't playing, do nothing
    if(this.state.gameOver || !this.state.entered || this.currAnimation){
      return;
    }

    //max items at 6
    if(this.state.build.length < 6){
       //add clicked item to build
        this.state.build.push(this.makeNewIngredient(event.target.id));
        this.setState({});
    }

    this.updateClickable();
  }

  /**
   * Removes ingredient from build stack.
   * 
   * @param pointer event from clicking on ingredient
   */
  removeIngredient = (event) => {
    //if game isn't playing, do nothing
    if(this.state.gameOver || !this.state.entered || this.currAnimation){
      return;
    }

    const res = event.target.id.split("-");
    const targetId = res[1];
    const len = this.state.build.length;
    if(len < 1){
      return;
    }
    //if in stack mode, only remove if it's the first ingredient
    if(this.state.mode == "Stack" && (this.state.build[len - 1].id == targetId)){
        this.state.build.splice(len-1, 1);
    } else if(this.state.mode == "Queue" && (this.state.build[0].id == targetId)){
        this.state.build.splice(0, 1);
    }

    this.updateClickable();
    this.setState({}); //rerender
  }

  /**
   * Calculates which ingredients are clickable according to the rules of stacks & queues.
   */
  updateClickable(){
    //if there are no items in the build, don't do anything!
    const buildLen = this.state.build.length;
    if (buildLen < 1){ return; }

    //remove all clickable tags
    this.state.build.forEach((ing) => ing.clickable = false);

    //if stack, only first ingredient is clickable (last in array)
    if(this.state.mode == "Stack"){
       this.state.build[this.state.build.length - 1].clickable = true;
    } else if (this.state.mode == "Queue") {
      //if queue, only last ingredient is clickable (first in array)
      this.state.build[0].clickable = true;
    }
  }

  /**
   * Starts animation of moving ingredients from build stack to plate stack. Called on "serve order" button click.
   */
  serveBurger(){
    //if not in animation mode
    if(!this.currAnimation){
      let username = this.props.username;
      const actionPayload = {
        burger: this.state.build.map((ing) => ing.name),
        username: username
      }
      sendAction(actionPayload);
    }
  }

 //gets starting info from game
  onStartResp(response){
    let parsedOrder = JSON.parse(response.payload.order);

    //set order and mode
    this.setState({
        order: parsedOrder,
        mode: response.payload.stackqueue,
        build: [],
        serve: []
    });
  }

  //called by backend when input is handled
onActionResp(response){
  let parsedOrder = JSON.parse(response.payload.order);
  let nextScoreVal = JSON.parse(response.payload.nextScoreVal);
    //update order and mode, clear build and serve stacks
      this.setState({
        score: response.payload.score,
        nextPoints:nextScoreVal
    })

    this.startServeAnimation(parsedOrder, response.payload.stackqueue, response.payload.valid);
}

onLeaveResp(response){
}

//starts animation where burger is remade 
startServeAnimation(newOrder, newMode, validOrder) {
   this.moveIngToPlate(newOrder, newMode, validOrder);
  this.currAnimation = setInterval(() => this.moveIngToPlate(newOrder, newMode, validOrder), 500);
}

/**
 *  Resposible for moving the ingredient from the build stack to the plate stack and updating the order.
 * @param {*} newOrder  : array of ingredients to replace old order
 * @param {*} newMode : mode of new order
 */
moveIngToPlate(newOrder, newMode, validOrder){
    //if last one, add score
    if(this.state.build.length == 0){
      clearInterval(this.currAnimation);
      this.currAnimation = null;
      //flash target red or green depending on correctness
      if(validOrder){
        flashBoxGreen(this.targetRef);
      } else {
        flashBoxRed(this.targetRef);
      }
      this.setState({
        order: newOrder,
        mode: newMode,
        build: [],
        serve: [],
        errorMsg: ""
    })
    } else {
      //move one item from build to plate
      if(this.state.mode == "Stack"){
        this.state.serve.unshift(this.state.build.pop().name);
      } else {
        this.state.serve.unshift(this.state.build.shift().name)
      }
       this.setState({});
    }
}

  render() {

    //show intro screen if game has yet to be entered
    let content;
    if(!this.state.entered){
      content = <GameIntroScreen title={this.state.name} instructions={this.instructionsText} 
                                submit={() => this.startGameBtnPressed()} dataStructure="Stacks and Queues" 
                                inputGraphics={this.inputGraphics} planetUrl={burgerPlanet} topOffset={this.PLANET_TOP_OFFSET} leftOffset={this.PLANET_LEFT_OFFSET} 
                                back={this.props.onLeave}/>
    } else {
        //IF GAME IS BEING PLAYED
      if(!this.state.gameOver){
       
        content = <div>
            {backButton(this)}
            {/* score and time*/}
          <div className="score-box">
                SCORE
              <h2 style={{fontSize: "32px"}}>{this.state.score}</h2>
          </div>

        <Timer ref={this.timer} startTime={this.state.startTime} onEnd={() => onGameOver(this)} />

        {/* /* ORDER */}
          <div ref={this.targetRef} className="target-div" style={{height: "400px"}} >
              <h2 className="target-label" style={{marginBottom: "0px"}} >ORDER</h2>
              <h2 className="target-label" style={{margin: "5px", fontSize: "28px"}} >{this.state.mode}</h2>
              <div className="order-div">
                     {this.state.order.map((name) => <div key={name+"-order"+this.state.serve.length+"."+Math.random()*100} className={"ingredient order-ing " + name}/>)}
              </div>
          </div>

           {/* BUILD AREA */ }
            <div className="build-burger-div">
                  <div className="build-burger-div-inner">
                         {this.state.build.map((ing) => <div key={ing.name+"-build"+ing.id} 
                            id={ing.name + "-" + ing.id}
                            className={"ingredient build-item " + ing.name + " " + (ing.clickable ? "clickable" : "")}
                            onPointerUp={this.removeIngredient}/>)}
                 </div>
            </div>

           {/* OUTPUT AREA */}
           <div className="output-div">
                <div className="output-div-inner">
                 {this.state.serve.map((name) => <div key={name+"-output"+this.state.serve.length+"."+Math.random()*100} className={"ingredient " + name}/>)}
                 <div className="ingredient plate" />
                 </div>
            </div>


            {/* INGREDIENTS */}
            <div className="ingGroup">
               {this.ingredientList.map((name) => <div key={name+"-ing"} id={name} className={"ingredient clickable " + name} onPointerUp={this.addIngredient}/>)}
            </div>

            <div id="error-msg-qg">{this.state.errorMsg}</div>

            {/* SUBMIT BTN */}
            <button id="serve-btn" onClick={() => this.serveBurger()}> SERVE ORDER </button>
            
            </div>

            document.getElementById("main-game-div").focus();  //fixes focus issues 
    } else {
        //IF GAME IS OVER
        content = <GameOverScreen title={this.state.name}
        replay={() => this.startGameBtnPressed()} back={this.props.onLeave} dataStructure="Stacks and Queues"
        planetUrl={this.planetImg} topOffset={this.PLANET_TOP_OFFSET} leftOffset={this.PLANET_LEFT_OFFSET} score={this.state.score} scoreUnits="burger orders completed"
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

export default QueueGame;