import React, {Component} from 'react';
import './css/App.scss';
import IntroScreen from "./IntroScreen";
import NavBar from "./NavBar";
import World from "./World";
import money from "./misc/cash.mp3";
import { instanceOf } from 'prop-types';
import background from "./misc/Urban-Jungle-2061_Looping.mp3"
import {CookiesProvider, withCookies, Cookies} from "react-cookie";
import CryptoJs from "crypto-js";


let interval;
let socket = "";

//corresponds to the enums in the WebSockets class
const MESSAGE_TYPE = {
    CONNECT: 0,
    LOGIN: 1,
    SIGNUP: 2,
    MOVE: 3,
    HEARTBEAT: 4,
    FORWARD: 5,
    GUEST: 6,
    ERROR: 7,
    GAME_START: 8,
    GAME_ACTION: 9,
    GAME_LEAVE: 10,
    LOGOUT: 11,
    CANBUY:12,
    SHOP:13,
    BUY:14,
    ROCKET_CHANGE: 15,
    JOIN_ARENA: 16,
    LEAVE_ARENA: 17,
    LOBBY_CREATE: 18,
    LOBBY_LEAVE: 19,
    LOBBY_INVITE: 20,
    LOBBY_LOAD_GAME: 21,
    INVITE_RESPONSE: 22,
    RANDOM_OPPONENT: 23,
    CHANGEUSER: 24,
    CHANGEPASS: 25,
    UPDATESCORE: 26,
    DESTROY_LOBBY: 66
};

/**
 * Models the top-level React component for this rexzzprogram.
 * Requires an enviroment that provides cookies.
 */
class App extends Component {

    static propTypes = {
        cookies: instanceOf(Cookies).isRequired
    };

    constructor(props) {
        super(props);

        this.state = {
            introscreen: true,
            username: "",
            coins: 0,
            rocket: "red",
            options: [],
            speed: 5,
            loggedIn: false,
            error: "",
            key: Math.random()
        };

        this.world_ref = React.createRef();
        this.background = new Audio(background);
        this.background.addEventListener('ended', function() {
            this.currentTime = 0;
            this.play();
        }, false);
        this.cashSound = new Audio(money);
    }

    hideIntro = () => {
        this.setState({introscreen: false, key: Math.random()});
        if(this.world_ref.current){
            this.world_ref.current.resetRocketPos();
        }

        document.getElementById("world-view").focus();

    };

    showIntroScreen = () => {
        this.setState({introscreen: true});
    };

    setupWebSocket = () => {
        socket = new WebSocket("ws://localhost:4567/socket"); //local
        // let host = window.location.href.replace(/^http/, 'ws') + "socket"; //deployed
        // socket = new WebSocket(host);

        socket.onmessage = (message) => {
            const response = JSON.parse(message.data);
            const type = response.type;

            switch(type) {
                case MESSAGE_TYPE.FORWARD:
                    if ("error" in response) {
                        this.setState({error: response.error});
                        return;
                    }

                    const payload = JSON.parse(response.payload);
                    this.hideIntro();
                    const rockets = JSON.parse(payload["rockets"]);
                    this.setState({options: rockets,
                        rocket: this.parseRockets(rockets),
                        username: payload["username"],
                        coins: parseInt(payload["coins"]),
                        speed: payload["speed"],
                        loggedIn: true
                    });
                    if(this.world_ref.current) {
                        this.world_ref.current.reset();
                    }
                    this.toggleTutorial(payload["showTutorial"]);
                    break;
                case MESSAGE_TYPE.CONNECT:
                    interval = setInterval(this.heartbeat, 2500);
                    break;
                case MESSAGE_TYPE.GAME_START:
                     this.world_ref.current.onStartResp(response);
                     break;
                case MESSAGE_TYPE.GAME_LEAVE:
                     this.updateStarbucks(response);
                     this.world_ref.current.onLeaveResp(response);
                     break;
                case MESSAGE_TYPE.GAME_ACTION:
                     this.world_ref.current.onActionResp(response);
                     break;
                case MESSAGE_TYPE.ROCKET_CHANGE:
                        const rocket_payload = response.payload;
                    this.setState({rocket: rocket_payload.color});
                    break;
                case MESSAGE_TYPE.SHOP:
                    break;
                case MESSAGE_TYPE.BUY:
                    const before = this.state.coins;
                    const beforeUser = this.state.username;


                    if ("error" in response) {
                        this.setState({error: response.error});
                        return;
                    }
                    const pay = JSON.parse(response.payload);
                    const rocks = JSON.parse(pay["rockets"]);
                    this.setState({options: rocks, rocket: this.parseRockets(rocks), username: pay["username"], coins: parseInt(pay["coins"]), speed: pay["speed"]});

                    if (this.state.coins !== before && (this.state.username === beforeUser)) {
                      this.cashSound.play();
                    }

                    break;
                case MESSAGE_TYPE.LOGOUT:
                    this.setState({loggedIn: false})
                    this.removeCookies();
                    this.showIntroScreen();
                    break;
                case MESSAGE_TYPE.LOBBY_CREATE:
                    break;
                case MESSAGE_TYPE.LOBBY_INVITE:
                    break;
                case MESSAGE_TYPE.LOBBY_LEAVE:
                    break;
                case MESSAGE_TYPE.LOBBY_LOAD_GAME:
                    break;
                case MESSAGE_TYPE.INVITE_RESPONSE:
                    break;

                default:
                    break;

            }

            const { cookies } = this.props;

            if (cookies.get("username") && !this.state.loggedIn) {
                const username = cookies.get("username");
                const password = cookies.get("password");
                const payload = {
                    username: username,
                    password: CryptoJs.SHA256(password).toString()
                };


                const toSend = {
                    type: MESSAGE_TYPE.LOGIN,
                    payload: payload
                };

                socket.send(JSON.stringify(toSend));
            }

        };
    };

    updateStarbucks(response){
        let pay = response.payload;
        let starbucks = pay["starbucks"]
        this.setState({
            coins:starbucks
        })
    }

    parseRockets = (rockets) => {
        for (let ele in rockets) {
            if (rockets[ele] === 2) {
                return ele;
            }
        }
    };

    removeCookies = () => {

        const { cookies } = this.props;
        cookies.remove("username");
        cookies.remove("password");
    };

    heartbeat = () => {
        const message = {
            type: MESSAGE_TYPE.HEARTBEAT,
        };
        socket.send(JSON.stringify(message));
    };

    componentDidMount() {
        const { cookies } = this.props;
        this.background.play()
            .catch(error => {
                this.background.play()

            });
        this.setupWebSocket();
        this.changeBackgroundVolume(0.5); //default




    }

    componentWillUnmount() {
        clearInterval(interval);
    }

    changeRocket = (color) => {
        this.setState({rocket: color})
    };

    changeBackgroundVolume = (volume) => {
        this.background.pause();
        this.background.volume = volume;
        this.background.play();
    };

    changeSoundsVolume = (volume) => {
        this.cashSound.volume = volume;
        if(this.world_ref.current){
            this.world_ref.current.setVolume(volume);
        }
    };

    toggleTutorial(doShow){
        //get current state
        if(this.world_ref.current){
            this.world_ref.current.toggleTutorial(doShow);
        }
    }

    setCookies = (username, password) => {
        const { cookies } = this.props;
        cookies.set('username', username, { path: '/' });
        cookies.set('password', password, { path: '/' });
    };

    render() {


        return (

            <CookiesProvider>

            <div className="App" onKeyDown={this.onKeyDown}>

                {this.state.introscreen ?

                    <IntroScreen removeCookies={this.removeCookies}
                        setCookies={this.setCookies}
                                 error={this.state.error}
                                 submit={this.hideIntro}/> :
                    <NavBar worldRef={this.world_ref}
                            rocket_options={this.state.options}
                            soundVolume={this.changeSoundsVolume}
                            backgroundVolume={this.changeBackgroundVolume}
                            change_rocket={this.changeRocket}
                            currRocket={this.state.rocket}
                            username={this.state.username}
                            coins={this.state.coins}/>

                }
               <World ref={this.world_ref}
                      rocket={this.state.rocket}
                      username={this.state.username}
                      speed={this.state.speed}
                      coins={this.state.coins}/>
            </div>
            </CookiesProvider>
        );
    }
}

export default withCookies(App);
export {socket, MESSAGE_TYPE,};
