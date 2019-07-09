import React, { Component } from 'react';
import './css/ArenaMenu.scss';
import {socket, MESSAGE_TYPE} from "./App";
import Lobby from "./Lobby";
import ReactLoading from 'react-loading';
import arena_planet from "./img/planets/arena_planet.png";
import Planet from "./Planet";

/**
 * Models the menu screen for the multiplayer arena.
 *
 * Expects the following props:
 *
 * @prop username, current player's username
 * @prop joinReqest, current request to join a player's lobby
 * @props game, current game
 * @props showGame, function to show a game
 *
 */
class ArenaMenu extends Component {
    constructor(props) {
        super(props);
        this.state = {
            lobby: false,
            random: false,
            joinRequest: null,
            join: false,
            foreignLobby: null,
            randomSearchers:0,
            gameMap: {
                0: "Bin-apple Trees",
                1: "Choco Chip Links",
                2: "Candy Hash Saga"
            }
        };

        this.lobbyRef = React.createRef();
    }

    componentDidMount() {
        const toSend = {
            type: MESSAGE_TYPE.JOIN_ARENA,
            username: this.props.username
        };
        socket.send(JSON.stringify(toSend));

        socket.addEventListener("message", (message) => {
            const parsed = JSON.parse(message.data);
            if (parsed.type === MESSAGE_TYPE.LOBBY_INVITE) {
                this.setState({
                    joinRequest: {
                    owner: parsed.owner
                    }})
            } else if (parsed.type === MESSAGE_TYPE.LOBBY_LOAD_GAME){
                    this.onLoadIntroScreenResponse(parsed);
            } else if(parsed.type===MESSAGE_TYPE.DESTROY_LOBBY){
                //return all players to the arena menu regardless of who sent the msg.
                this.setState({
                    lobby: false,
                    random: false,
                    joinRequest: null,
                    join: false,
                    foreignLobby: null
                });
            }else if(parsed.type===MESSAGE_TYPE.RANDOM_OPPONENT){
                this.setState({
                    randomSearchers:parsed.numPlayers
                })
            }
        });
    }

    componentWillReceiveProps(nextProps, nextContext) {
        if (nextProps.joinRequest !== this.props.joinRequest) {
            this.setState({joinRequest: true});
        }
    }

    createLobby = () => {
        this.setState({lobby: true});
        const toSend = {
            type: MESSAGE_TYPE.LOBBY_CREATE,
            username: this.props.username
        };
        socket.send(JSON.stringify(toSend));
    };

    randomGame = () => {

        const toSend = {
            type: MESSAGE_TYPE.RANDOM_OPPONENT,
            join: true //you want to join the queue
        };
        socket.send(JSON.stringify(toSend));
        this.setState({random: true});
    };

    acceptJoin = () => {
        const toSend = {
            type: MESSAGE_TYPE.INVITE_RESPONSE,
            lobbyOwner: this.state.joinRequest.owner,
            username: this.props.username,
            accepted: true
        };
        socket.send(JSON.stringify(toSend));
        this.setState({
            join: true, //DO WE NEED THIS?
            foreignGame: this.props.game,
            lobby: false});
    };

    onLoadIntroScreenResponse(response) {
        let resp = response;
        let ready = resp.ready;

        //if we're ready
        if(ready) {
            //display intro screen
            let game = this.props.showGame(this.state.gameMap[parseInt(response.gameType)], true);
            //let game = this.props.showGame(this.state.gameMap[0], true);


            //wait 5 seconds for game to start
            setTimeout(() => {
                //enterGame(game);
                const toSend = {
                    type: MESSAGE_TYPE.GAME_START,
                    username: this.props.username,
                    gameName: this.state.gameMap[parseInt(response.gameType)]
                  //  gameName: this.state.gameMap[0]
                };
                socket.send(JSON.stringify(toSend));

                //visuals
                game.setState({
                    entered: true, //showing the playable game
                    gameOver: false,
                    score: 0
                });

            }, 5000);
        } else {
            //nothing happens currently but there should be a visual indication
        }


    }
    declineJoin = () => {
        const toSend = {
            type: MESSAGE_TYPE.INVITE_RESPONSE,
            lobbyOwner: this.state.joinRequest.owner,
            username: this.props.username,
            accepted: false
        };
        socket.send(JSON.stringify(toSend));
        this.setState({join: false, joinRequest:  null});

    };

    returnToArenaMenu = () => {
        this.setState({
            lobby: false,
            random: false,
            joinRequest: null,
            join: false,
            foreignLobby: null
        });
        //send a LOBBY_LEAVE message
        const toSend = {
            type: MESSAGE_TYPE.LOBBY_LEAVE,
            lobbyOwner: this.state.joinRequest? this.state.joinRequest.owner: this.props.username,
            leaver: this.props.username,
        };
        socket.send(JSON.stringify(toSend));
    };

    leaveMatchmaking = ()=>{
        this.setState({
            random:false
        });
        const toSend = {
            type: MESSAGE_TYPE.RANDOM_OPPONENT,
            join: false, //do this to signal that you are exiting
        };
        socket.send(JSON.stringify(toSend));
    };

    backToSpace = () => {
        const toSend = {
            type: MESSAGE_TYPE.LEAVE_ARENA,
            username: this.props.username
        };
        socket.send(JSON.stringify(toSend));
        this.props.onLeave();
    };
    render() {

        if (this.state.lobby) {
            return <Lobby ref={this.lobbyRef} creator={this.props.username} back={this.returnToArenaMenu} access={1} showGameFunction={this.props.showGame}/>;
        } else if (this.state.join) {
            return <Lobby ref={this.lobbyRef} creator={this.state.joinRequest.owner } back={this.returnToArenaMenu} access={0} showGameFunction={this.props.showGame}/>;
        } else  if (this.state.random) {
            return <div className={"random arena"}>
                 <Planet top={-40} left={-110} width={220} rotate={-10} imgUrl={arena_planet} name={""}/>
                <h2 id="random-title">Matching you with another player </h2>
                <div className={"loading"}> <ReactLoading type={"spinningBubbles"} color={"white"} width={150} height={150}/></div>
                <h3 id="numPlayers">{this.state.randomSearchers} players looking for a random opponent </h3>
                <button className={"arena-button"} id={"random-cancel"} onClick={this.leaveMatchmaking} >Cancel</button>
               </div>
        } else return (
            <div className="ArenaMenu arena">
                {this.state.joinRequest ? <div className={"join"}>
                    <h2> You've been invited to {this.state.joinRequest.owner}'s lobby! </h2>
                    <button className={"x-button"} onClick={this.declineJoin}>X</button>
                    <button className={"join-button arena-button"} onClick={this.acceptJoin}>Join</button>
                    <button className={"join-button arena-button"} onClick={this.declineJoin}>Decline</button>

                </div>: null }
                <Planet top={-40} left={-110} width={220} rotate={-10} imgUrl={arena_planet} name={""}/>
                <h1 className={"arena-title"}>Enter the Arena</h1>
                <h3 className="arena-sub" id="main-arena-sub">Compete against other players in a random game! </h3>
                <h3 className="arena-sub" id="minor-arena-sub">Create a game and invite other players in the arena or hang out here to wait for an invitation!</h3>

                <div className={"wrapper"}>
                    <div className={"search-div"}>
                        <button className={"arena-button"} onClick={this.createLobby} >Create a Game</button>
                    </div>
                    <div className={"random-div"}>
                        <button className={"arena-button"} onClick={this.randomGame}>Random Opponent</button>
                    </div>
                        <div className={"leave"}>
                            <button className={"arena-button"} onClick={this.backToSpace}>Back to Space</button>
                        </div>
                    </div>
            </div>
        );
    }}

export default ArenaMenu;
