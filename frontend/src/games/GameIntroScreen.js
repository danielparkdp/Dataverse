import React, { Component } from 'react';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/GameIntroScreen.scss';
import Planet from '../Planet';
import DataStructureInfo from "../DataStructureInfo";

/**
 * Intro screen for a game. Expects the following props:
 *    @prop title : name of game (string)
 *    @props dataStructure : name of dataStructure that is being taught (string)
 *    @props inputGraphics : a list of DOMs to be displayed as the instructions for the game (can be img, div, etc)
 *    @props instructions: instructions text (string) displayed above inputGraphics
 *    @props leftOffset : left offset for planet
 *    @props topOffset : top offset for planet
 *    @props width: width of planet
 *    @props planetUrl: url to planet img
 *    @props showButtons: bool -> true if you want play button to show
 *    @props submit: () => {} function called when urser presses play button
 *    @props back: () => {} function called when user presses back to space button
 */
class GameIntroScreen extends Component {

  constructor(props) {
    super(props);

    this.state = {
        title: "",
        instructions: "",
        planetUrl: "",
        dataStructure: "",
        inputMap: {},
        topOffset: 0,
        infoToggled:false
    };
}

toggleInfo=()=>{
    this.setState((prevState) => ({
      infoToggled:!prevState.infoToggled
    }));
}

  render() {

    if(this.state.infoToggled){
      return <DataStructureInfo onClose = {this.toggleInfo} structure = {this.props.dataStructure}/>
    }else{

    return (
      <div className="intro-screen"  onKeyDown={this.onKeyDown}>

        <h1 className="game-title" >{this.props.title} </h1>
        <h2 className="data-struct" onClick={this.toggleInfo}> Data Structure: {this.props.dataStructure}</h2>
        <Planet top={30 + this.props.topOffset} left={-70 + this.props.leftOffset} width={this.props.width} imgUrl={this.props.planetUrl} name={""}/>
        <h3 className="instructions">{this.props.instructions} </h3>

        {/* GAME SPECIFIC INSTRUCTIONS */}
        <div className="instructionsDiv">{this.props.inputGraphics}</div>

         {/* SHOW BUTTONS IF PROPS SAYS TO */}
         {this.props.showButtons === undefined || this.props.showButtons ?
              <div>
                <button className={"large-button"} id="play-btn" onClick={this.props.submit}> Play Game </button>
                <button className={"large-button"} id="return-btn-intro" onClick={this.props.back}> Back to Space </button>
              </div>
                    : <div className={"intro-screen-msg"}> The game will start shortly. </div>}
            </div>
    );
         }
  }
}

export default GameIntroScreen;
