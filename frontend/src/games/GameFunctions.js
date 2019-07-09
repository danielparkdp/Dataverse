import React, { Component } from 'react';
import {socket, MESSAGE_TYPE} from "../App";

/**
 * Static functions that can be used by any game.
 */

//input codes
export const INPUT = {
    LEFT: 1,
    RIGHT: 2,
    UP: 3,
    ENTER: 4
};

//tells backend that this game is starting
export function startBackEnd(gameName, userName){
    let toSend = {
       type: MESSAGE_TYPE.GAME_START,
       gameName: gameName,
       username: userName
   };
   socket.send(JSON.stringify(toSend));
 }

//STARTS THE GAME
export function enterGame(game){
    startBackEnd(game.state.name, game.props.username);

    game.setState({
      entered: true,
      gameOver: false,
      score: 0
    });
}

 //sends input to backend
export function handleInput(dir, userName){
    const payload = {
        moveCode: dir,
        username: userName
    };
    sendAction(payload);
 }

  //called when timer finishes
 export function onGameOver(game) {
   //send leave message to backend
   let toSend = {
      type: MESSAGE_TYPE.GAME_LEAVE,
      username: game.props.username,
      gametype: game.state.name,
      highscore: game.state.score

    };
    socket.send(JSON.stringify(toSend));

    socket.addEventListener("message", (message) => {
        const parsed = JSON.parse(message.data);
        if (parsed.type===MESSAGE_TYPE.UPDATESCORE){
            game.setState({
              highScore: parsed["value"]
            });
        }
    });
    //game.setState({gameOver: true});
 }

 //animation for flashing arrows when key press
 export function flashArrow(arrow, className) {
     arrow.classList.add(className);
     setTimeout(function() {
        arrow.classList.remove(className);
    }, 200);
 }

 //sends action payload to backend
 export function sendAction(actionPayload){
    let toSend = {
        type: MESSAGE_TYPE.GAME_ACTION,
        actionPayload: actionPayload
    };
    socket.send(JSON.stringify(toSend));
 }

 //refocuses on proper elements if game is in progress - fixes focus issues in minigames
 export function gameOnBlur(game){
    if (game.state.entered && !game.state.gameOver){
      document.getElementById("main-game-div").focus();
    };
  }

  //flashes input ref to div red
  export function flashBoxRed(refToBox){
    if(refToBox!=null && refToBox.current!=null){
    refToBox.current.classList.add("flash-box-red");
    setTimeout(() => {if(refToBox.current) refToBox.current.classList.remove("flash-box-red")}, 400);
    }
  }

   //flashes input ref to div green
  export function flashBoxGreen(refToBox){
    if(refToBox!=null && refToBox.current!=null){
    refToBox.current.classList.add("flash-box-green");
    setTimeout(() => {if(refToBox.current) refToBox.current.classList.remove("flash-box-green")}, 400);
    }
  }

  //makes a back button to exit out of game
  export function backButton(game){
    return game.state.multiplayer ? undefined : <div className="back-button" onPointerUp={() => {game.setState({entered: false })}}/>
  }

    //flashes any ref to a div to have a red border
    export function flashScoreRed(refToScore){
      if(refToScore !=null && refToScore.current != null){
        refToScore.current.classList.add("flash-score-red");
        setTimeout(() => {if(refToScore.current) refToScore.current.classList.remove("flash-score-red")}, 400);
      }
    }
