import React, { Component } from 'react';
import './css/ArenaMenu.scss';
import {socket, MESSAGE_TYPE} from "./App";
import PlayerTag from "./PlayerTag";
import arena_planet from "./img/planets/arena_planet.png";
import Planet from "./Planet";

/**
 * Models the lobby screen in the multiplayer Arena.
 *
 * Expects the following props:
 *  @prop creator, creator of the lobby
 *  @prop back, function to exit out of the lobby
 */
class Lobby extends Component {
    constructor(props) {
        super(props);
        this.state = {
            players: [this.props.creator],
            ready: false,
            waiting: {},
            playerTags: {},
            submitError: ""
        }
    }

    componentDidMount() {
        socket.addEventListener("message", (message) => {
            const parsed = JSON.parse(message.data);
            if (parsed.type === MESSAGE_TYPE.INVITE_RESPONSE) {
                const waiting = this.state.waiting;
                waiting[parsed.user] = false;
                let ready = true;
                for (let key in waiting) {
                    if (waiting[key]) {
                        ready = false;
                    }
                }

                this.setState({waiting: waiting, ready:ready});
                if (parsed.accepted) {
                    const playerTags = this.state.playerTags;
                    playerTags[parsed.user] = <PlayerTag change={this.removePlayer} name={parsed.user} ready={true}/>;
                    this.setState({playerTags: playerTags});
                } else {
                    this.removePlayer(parsed.user);
                }



        } else if (parsed.type === MESSAGE_TYPE.LOBBY_INVITE) {

                if (parsed.valid) {
                    const players = this.state.players;
                    players.push(parsed.invitee + " ");
                    const playerTags = this.state.playerTags;
                    playerTags[parsed.invitee] = (<PlayerTag change={this.removePlayer} name={parsed.invitee} ready={false}/>);
                    this.setState({players: players});
                } else {
                    this.setState({submitError: parsed.errorReason});
                }

        }else if (parsed.type===MESSAGE_TYPE.LOBBY_LEAVE){
            this.removePlayer(parsed.leaver);
        }
        });
    }

    addPlayer = (event) => {
        event.preventDefault();
        this.setState({submitError: ""});
        const name = [event.target[0].value];
        if (name[0] === "") {
            return;
        } else if (name[0] === this.props.creator) {
            this.setState({submitError: "Error: you can't add yourself to your own lobby"});
            return;

        }
        document.getElementById("lobby-form").reset();

        const waiting = this.state.waiting;
        waiting[name] = true;
        const playerTags = this.state.playerTags;

        this.setState({waiting: waiting});
        const toSend = {
            type: MESSAGE_TYPE.LOBBY_INVITE,
            lobbyOwner: this.props.creator,
            invitee: name
        };



        socket.send(JSON.stringify(toSend));
    };

    startGame = () => {
        const toSend = {
            type: MESSAGE_TYPE.LOBBY_LOAD_GAME,
            lobbyOwner: this.props.creator,
        };

        socket.send(JSON.stringify(toSend));

       // alert("Start game");
    };


    removePlayer = (player) => {
        if (player === this.props.creator) {
            return;
        }

        let players = this.state.players;
        let playerTags = this.state.playerTags;
        playerTags[player] = null;
        players = players.filter((ele) => {return (ele !== player)});
        this.setState({players: players, playerTags: playerTags});
    };

    render() {
        const playerTags = [];
        for (let key in this.state.playerTags) {
            playerTags.push(this.state.playerTags[key]);
        }
        return (
            <div className="Lobby arena">
               <Planet top={-40} left={-110} width={220} rotate={-10} imgUrl={arena_planet} name={""}/>
                <h1 className={"arena-title lobby-title"}>{this.props.creator}'s Lobby</h1>
                {this.props.access ? <div className={"creator"}>
                    <form id={"lobby-form"} onSubmit={this.addPlayer}>
                    <input id={"lobby-input"} autoComplete={"off"} type={"text"} placeholder={"Enter players..."}/>
                    <input type={"submit"} value={"Add player"}/>
                    </form>
                    <div id={"submit-error"}> {this.state.submitError}</div>

                    <div className={"players"}>
                        {playerTags}
                    </div>

                    {this.state.ready ?
                        <button className={"arena-button"} onClick={this.startGame}>Start Game</button>:
                        <button className={"arena-button not-ready"}>Start Game</button>
                    }
                    <button className={"leave-lobby arena-button"} onClick={this.props.back}>Leave Lobby</button>
                </div> : <div className={"waiting"}> Waiting for other players...
                    <button className={"leave-lobby arena-button"} onClick={this.props.back}>Leave Lobby</button>
                </div>


                }

            </div>


        );
    }}

export default Lobby;
