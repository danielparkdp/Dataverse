import React, { Component } from 'react';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/GameIntroScreen.scss';
import '../css/GameOverScreen.scss';
import Planet from '../Planet';

/**
 * Intro screen for a game. Expects the following props:
 *    @prop title : name of game (string)
 *    @props dataStructure : name of dataStructure that is being taught (string)
 *    @props leftOffset : left offset for planet
 *    @props topOffset : top offset for planet
 *    @props width: width of planet
 *    @props planetUrl: url to planet img
 *    @props replay: () => {} function called when user presses replay button
 *    @props back: () => {} function called when user presses back to space button
 *    @props backToArena: () => {} function called when user presses back to arena button
 *    @props multiplayer: bool -> true if the game had > 1 player
 *    @props players: map of all the players' usernames to their score
 */
class GameOverScreen extends Component {

  constructor(props) {
    super(props);

    this.props = {
        score: 0,
        scoreUnits: "targets collected",
        replay: undefined, //function to replay game
        back: undefined, //function for back to space
    };
    this.state = {
        title: "",
        planetUrl: "",
        dataStructure: "",
        topOffset: 0 //for planet img positioning
    };

    this.myRank = undefined;
    this.playerRankMap = {};
}

/**
 * Returns the DOM to display the score, based on whether we are in multiplayer or single player mode
 * */
getScoreDisplay(sortedPlayers){
    //add scores to flex box if multiplayer
    if(this.props.multiplayer && this.props.players){
      //find ranks of the players   
       return <div className="score-wrapper"> <div className="multiplayer-score-div">
        { sortedPlayers.map((username) => 
                <div key={username+"score-go"} className="multiplayer-score-box"> 
                    <div className="mult-rank"> #{this.playerRankMap[username]} </div> 
                    <div className="mult-username"> {username} </div> 
                    <div className="mult-score">{this.props.players[username]["score"]} pts</div>
               </div>) }
       </div> </div>
    } else {
      //single player display
        return <div> 
          <div id="curr-score-display">
        <h3 className="game-over-score"> SCORE: </h3>
        <h3 className="game-over-num"> {this.props.score} </h3>
        </div>
        <div id="high-score-display">
        <h3 className="game-over-score"> HIGHSCORE: </h3>
        <h3 className="game-over-num"> {this.props.highScore} </h3>
        </div>

    </div>
    }
}

  /**
   * Returns the message to be displayed in the green banner.
   */
  getMsg(sortedPlayers){
    //multiplayer should be something like "You got 1st place, username!"
    if(this.props.multiplayer){
      //CALCULATE RANK
      let scores = new Set();
      let currRank = 0;
      sortedPlayers.forEach((player) => {
        let score = this.props.players[player]["score"];
        if(!scores.has(score)){
          scores.add(score);
          currRank++;
        }
        this.playerRankMap[player] = currRank;
      })
      //find proper English ending for rank
      let myRank = this.playerRankMap[this.props.username];
      let ending = "";
      switch(myRank){
        case 1:
           ending = "st";
           break;
        case 2:
           ending = "nd";
           break;
        case 3:
           ending = "rd";
           break;
        default:
           ending = "th";
           break;
      }
      return "You got " + myRank + ending + " place, " + this.props.username + "!";
    } else { //single player should be "Nice job, username!"
      return "Nice job, " + this.props.username + "!";
    }
  }

  render() {
     
    //sort all the given players if multiplayer
    let sortedPlayers = this.props.multiplayer ? 
             Object.keys(this.props.players).sort((user, user2) => 
                  (this.props.players[user]["score"] > this.props.players[user2]["score"]) ? -1 : 1) 
                   : undefined;

    return (
      <div className="intro-screen exit-screen"  onKeyDown={this.onKeyDown}>

        <h1 className="game-title" >{this.props.title} </h1>
        <h2 className="data-struct">Data Structure: {this.props.dataStructure}</h2>
        <Planet top={30 + this.props.topOffset} left={-70 + this.props.leftOffset} width={this.props.width ? this.props.width : 220} imgUrl={this.props.planetUrl} name={""}/>

        <h2 className="msg"><em>{this.getMsg(sortedPlayers)}</em></h2>

          {/* SCORE */}
         {this.getScoreDisplay(sortedPlayers)}

         {/* IF MULTIPLAYER, REPLAY BUTTON SHOULD BE RETURN TO ARENA */}
         {this.props.multiplayer == false ? 
           <button className={"large-button"} id="replay-btn" onClick={this.props.replay}> Replay </button> 
           :  <button className={"large-button"} id="replay-btn" onClick={this.props.backToArena}> Back to Arena </button> }
        
         <button className={"large-button"} id="return-btn" onClick={this.props.back}> Back to Space </button>
      </div>
    );
  }
}

export default GameOverScreen;