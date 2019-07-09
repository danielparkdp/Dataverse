import React, { Component } from 'react';
import '../css/Game.scss';

/**
 * Displays all players scores in a box. 
 * 
 * Expects props:
 *   @prop players : map of players to score
 */
class StandingsDisplay extends Component {

    constructor(props) {
        super(props);
    }

    render() {

        return (
            <div className="opponent-score-div" id={this.props.cssid}>
                  <b>STANDINGS</b>
                  {Object.keys(this.props.players).map((username) => <div key={username+"score-d"}> 
                            {username} : {this.props.players[username]["score"]} </div>)}
            </div>
        );
    }
}

export default StandingsDisplay
