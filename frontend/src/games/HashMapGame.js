import React, { Component } from 'react';
import candy from '../img/hashmap/candy_blue.png';
import candyPlanet from '../img/planets/candy_planet.png';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/HashMapGame.scss';
import '../css/BinaryTreeGame.scss';
import GameIntroScreen from './GameIntroScreen';
import BTPreview from './BTPreview';
import Timer from './Timer';
import GameOverScreen from './GameOverScreen';
import { enterGame, onGameOver, sendAction, gameOnBlur, flashBoxGreen, flashBoxRed, backButton, flashScoreRed} from './GameFunctions';
import StandingsDisplay from './StandingsDisplay';
import instr_1 from '../img/hashmap/hashmap_instructions1.png';
import instr_2 from '../img/hashmap/hashmap_instructions2.png';
import instr_3 from '../img/hashmap/hashmap_instructions3.png';
import instr_4 from '../img/hashmap/hashmap_instructions4.png';
import white_arr from '../img/white_right_arrow.png';

/**
 * HASH MAP GAME:
 * The player finds target candies by mod-ing their key value and searching through the corresponding bucket.
 *
 *  MULTIPLAYER COMPATABLE
 *
 * Expects the following props:
 *          @prop username : username of current player
 *          @prop onLeave : function called when player exits game
 *          @prop backToArena: function called when
 */
class HashMapGame extends Component {

    PLANET_OFFSET_TOP = -35;
    PLANET_OFFSET_LEFT = -55;
    PLANET_WIDTH = 260;
    DATA_STRUCTURE_NAME = "HashMap";

    constructor(props) {
        super(props);
        //state
        this.state = {
            name: "Candy Hash Saga",
            targetVal: 14,
            modVal: 5,
            score: 0,
            startTime: 60,
            entered: false,
            gameOver: false,
            currBucketContent: [],
            players: [],
            playerStateMap: {},
            multiplayer: false,
            highScore: 0,
            missedGuess:false, //whether a recent guess was wrong.
            nextPoints:100, //the number of points you will get for getting the next thing right.
        };

        //refs
        this.mainGame = React.createRef();
        this.timer = React.createRef();
        this.bucketContentsRef = React.createRef();
        this.targetBox = React.createRef();
        this.scoreRef = React.createRef();

        //instructions for intro screen
        this.instructionsText = "Find as many candies as you can by hashing numbers into groups!";
        this.inputGraphics = [<img key="hmi-1" src={instr_1} className="instructionsPic"/>,
                            <img key="hmi-2" src={instr_2} className="instructionsPic"/>,
                            <img key="hmi-3" src={instr_3} className="instructionsPic"/>,
                            <img key="hmi-4" src={instr_4} className ="instructionsPic"/>];
        this.playerStateMap = {};
       }

/**
 * Gets starting info from backend and sets up game with current node value, target, and number of players
 *
 * @param response response package from backend
 * */
onStartResp(response){
     this.playerStateMap = {};
     let parsedPlayers = JSON.parse(response.payload.players);

      //reset all stored player states
      this.setState({playerStateMap: {}});
      parsedPlayers.forEach(username => this.state.playerStateMap[username] = {score: 0});
      this.setState({}); //rerender

     this.setState({
       targetVal: response.payload.target,
       currBucketContent: [],
       players: parsedPlayers,
       modVal: response.payload.numBuckets
   })
}

/**
 * When a user makes a move, backend will call this with results from move.
 *
 * @response backend response
 */
onActionResp(response){
  //unpack what the backend sent
  let payload = response.payload;
  let valid = payload.valid;
  let parsedPlayers = JSON.parse(payload.players);
  let oldScore = this.state.playerStateMap[payload.userWhoMoved]["score"];
  let score = parsedPlayers[this.props.username]["score"];

  //update state
  this.setState({
      score: score,
      targetVal: payload.target,
   });

   //for each player, update graphics to represent changes
  Object.keys(parsedPlayers).forEach((username) => {
    //UPDATE BT PREVIEW AND SCORE
    this.state.playerStateMap[username]["score"] = parsedPlayers[username]["score"];
  });

  //if this info is FOR THIS USER, this player moved
    if(this.props.username === payload.userWhoMoved) {
      this.updateForCurrentPlayer(payload);
    } else {
      //if another player got it, flash it red
      if(oldScore < parsedPlayers[payload.userWhoMoved]["score"]){
          flashBoxRed(this.targetBox);
      }
    }

  this.setState({}); //rerender
}

/**
 * Updates graphics and animations for current players' move.
 *
 * @param payload payload of backend response
 */
updateForCurrentPlayer(payload){
  let parsedPlayers =  JSON.parse(payload.players);
  let bucketContent = JSON.parse(parsedPlayers[this.props.username].currBucketNums);
  let valid = payload.valid;
  let newScoreVal = parsedPlayers[this.props.username]["newScoreVal"];
  this.setState({
    nextPoints:newScoreVal
  });
  //if bucket was pressed
  if(bucketContent && this.lastBucketClicked) {

    this.deselectAllBuckets();

    //if wrong bucket was selected, flash score red
    let rightBucket = this.state.targetVal % this.state.modVal == this.lastBucketClicked;
    if(!rightBucket){
      this.setState({
        missedGuess:true
      });
      setTimeout(() => {
        this.setState({
          missedGuess:false
        });
    }, 400);
    }

    //if we want the bucket to turn red
    let colorClass = valid ? "active" : "wrong-active";

    //active this bucket with color determined above
     this.bucketRefs.forEach((bucket) => {
          if(bucket.current.innerHTML === this.lastBucketClicked) {
              bucket.current.classList.add(colorClass + "-bucket");
              return;
          }
       });

    //show bucket contents
      this.bucketContentsRef.current.classList.add(colorClass + "-border");
      this.setState({
          currBucketContent: bucketContent
      });

  //if candy was pressed
  } else {
      //some graphical representation of correctness or incorrectness
      if(valid){
        flashBoxGreen(this.targetBox);
        this.deselectAllBuckets();
      } else {
        flashBoxRed(this.targetBox);
      }
  }
}

/**
 * Deselects all buckets and clears current content bucket div.
 */
deselectAllBuckets() {
    //deactive other buckets
    this.bucketRefs.forEach((bucket) => {
        bucket.current.classList.remove("active-bucket");
        bucket.current.classList.remove("wrong-active-bucket")
    });

    this.bucketContentsRef.current.classList.remove("active-border");
    this.bucketContentsRef.current.classList.remove("wrong-active-border");

    //clear bucket contents
    this.setState({currBucketContent: []});
}

//fixes focus issue
componentDidMount() {
  document.getElementById("main-game-div").focus();
}

/**
 * KEY EVENTS:
 *   @esc : leave game
 */
onKeyDown = (event) => {
  //if game isn't playing, do nothing
  if(this.state.gameOver || !this.state.entered){
    return;
  }

  switch(event.keyCode){
    case 27: //esc
        onGameOver(this);
       break;
    default:
       break;
  }
}

/**
 * POINTER EVENTS: bucket clicked
 * Toggle visibility of bucket contents
 * */
bucketClicked(event) {

    //if already active, de-active
    if(event.target.classList.contains("active-bucket")){
        event.target.classList.remove("active-bucket");
        this.bucketContentsRef.current.classList.remove("active-border");
        event.target.classList.remove("wrong-active-bucket");
        this.bucketContentsRef.current.classList.remove("wrong-active-border");
    } else {
        //send message to backend to validate if this is the correct bucket
        this.lastBucketClicked = event.target.innerHTML;
        let username = this.props.username;
        const payload = {
            newBucket: event.target.innerHTML,
            username: username,
            moveType: 0 // for showing a bucket
        };
        sendAction(payload);
    }
}

/**
 * POINTER EVENTS: candy clicked
 *     send action payload to backend with selected candy
 * */
candyOnClick(val){
  this.lastBucketClicked = undefined;
    //send message to backend to validate if this is the correct bucket
    let username = this.props.username;
    const payload = {
        guess: val,
        username: username,
        moveType: 1 // for showing a bucket
    };
    sendAction(payload);
}

/**
 * Creates a list of buckets according to the mod value
 */
createBuckets() {
    let buckets = [];
    this.bucketRefs = [];
    for(let i = 0; i < this.state.modVal; i++){
        const id = "bucket-" + String(i);
        const ref = React.createRef();
        buckets.push(<div key={id} className="bucket" ref={ref} onPointerUp={(event) => this.bucketClicked(event)}>{i}</div>);
        this.bucketRefs.push(ref);
    }
    return buckets;
}

  render() {
    //show intro screen if game has yet to be entered
    let content;
    if(!this.state.entered){
      content = <GameIntroScreen title={this.state.name} instructions={this.instructionsText}
                                submit={() => enterGame(this)} dataStructure={this.DATA_STRUCTURE_NAME}
                                inputGraphics={this.inputGraphics} planetUrl={candyPlanet}
                                topOffset={this.PLANET_OFFSET_TOP} leftOffset={this.PLANET_OFFSET_LEFT} width={this.PLANET_WIDTH} back={this.props.onLeave}
                                showButtons={!this.state.multiplayer}/>
    } else {
        //IF GAME IS BEING PLAYED
      if(!this.state.gameOver){

        content = <div>
           {backButton(this)}
        {/* /* target window */}
          <div className="target-div" ref={this.targetBox} style={{height: "260px"}}>
              <h2 className="target-label" ref={this.mainGame} >TARGET</h2>
              <div className="target" style={ { backgroundImage:`url(${candy})`, width:"180px", height: "120px", lineHeight:"120px", marginTop: "0px"}}> {this.state.targetVal} </div>
               {/* popup for missing score or just a reminder of point value you will earn next.*/}
              <div className={"next-points-div " + (this.state.missedGuess ? "missed-points-div" : "")}>
                REWARD
              <h1>{this.state.nextPoints}</h1>
            </div>
          </div>

          {/* score and time*/}
          <div className="score-box" ref={this.scoreRef}>
                SCORE
              <h2 style={{fontSize: "32px"}}>{this.state.score}</h2>
          </div>

           {/* Show other players' scores if this is multiplayer */}
        {this.state.playerStateMap && this.state.players.length > 1 ? <StandingsDisplay cssid="hashmap-standings" players={this.state.playerStateMap}/> : undefined }

        <Timer ref={this.timer} startTime={this.state.startTime} onEnd={() => onGameOver(this)} />

        {/* WHAT WE ARE MOD-ING BY */}
        <img className="white-arr" src={white_arr} />
        <div className="score-box" id="mod-box" >
         HASH FUNCTION: <div style={{fontSize:"40px", lineHeight:"80px"}}> % {this.state.modVal}</div>
        </div>

       {/* BUCKETS */}
       <div id="remainder-label"> REMAINDER </div>

       <div className="bucket-outer-div">
            <div className="bucket-group">
                    {this.createBuckets()}
            </div>

            {/* CONTENT OF SELECTED BUCKET */}
            <div className="bucket-group bucket-contents" ref={this.bucketContentsRef}>
                    {this.state.currBucketContent.map((val) =>
                    <div className="candy-wrapper"  onPointerUp={() => this.candyOnClick(val)}>
                      <div className="candy-text"> {val} </div>
                        <div key={"bucket-content-" + val} className="candy" >
                              <img className="candy-img" src={candy}/>
                        </div>
                      </div>)}
            </div>

       </div>

            </div>

            document.getElementById("main-game-div").focus();  //fixes focus issues
    } else {
        //IF GAME IS OVER
        content = <GameOverScreen title={this.state.name}
        replay={() => enterGame(this)} back={this.props.onLeave} dataStructure={this.DATA_STRUCTURE_NAME}
        planetUrl={candyPlanet} topOffset={this.PLANET_OFFSET_TOP} leftOffset={this.PLANET_OFFSET_LEFT} width={this.PLANET_WIDTH} score={this.state.score}
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

export default HashMapGame;
