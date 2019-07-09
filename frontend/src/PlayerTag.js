import React, { Component } from 'react';
import './css/PlayerTag.scss';

/**
 * Models the tag representing a player.
 * Expects the following props:
 * @prop ready, whether or not the player is ready
 * @prop name, the player's username.
 */
class PlayerTag extends Component {




    render() {
        let ready = "player-tag-wrapper not-ready";
        if (this.props.ready) {
            ready = "player-tag-wrapper ready"
        }
        return ( <div className={ready}>
            <p className={"player_name"}><span className={"name"}> {this.props.name} </span></p>
            </div>

        );
    }
}

export default PlayerTag;