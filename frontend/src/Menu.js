import React, { Component } from 'react';
import './css/Menu.scss';
import {socket, MESSAGE_TYPE} from "./App";
import CryptoJs from 'crypto-js';


/**
 * Models the settings menu for the game.
 *
 * Expects the following props:
 * @prop name, the player's name
 * @prop backgroundVolume, function to change the background volume
 * @prop soundVolume, function to change the volume for game sounds
 * @prop close, function to close the menu.
 */
class Menu extends Component {

    constructor(props) {
        super(props);
        this.state = {
            showUsername: false,
            showPassword: false,
            showAudio: false,
            showMenu: true,
            bgVol: 50,
            soundVol: 50,
        }
    }

    logout = () => {
        const toSend = {
            type: MESSAGE_TYPE.LOGOUT,
            payload: {username: this.props.name},

        };
        socket.send(JSON.stringify(toSend));
    };

    showUsername = () => {
        document.getElementById("Menu").focus();
        this.setState({showUsername: !this.state.showUsername, showMenu: !this.state.showMenu});
    };

    showPassword = () => {
        document.getElementById("Menu").focus();
        this.setState({showPassword: !this.state.showPassword, showMenu: !this.state.showMenu});
    };

    showAudio = () => {
        document.getElementById("Menu").focus();
        this.setState({showAudio: !this.state.showAudio, showMenu: !this.state.showMenu});
    };



    resetUsername = (event) => {
        event.preventDefault();

        const username = event.target[0].value;
        const password = event.target[1].value;
        const newUsername = event.target[2].value;

        const toSend = {
            type: MESSAGE_TYPE.CHANGEUSER,
            payload: {olduser: username, password: CryptoJs.SHA256(password).toString(), username: newUsername},

        };
        socket.send(JSON.stringify(toSend));

    };

    resetPassword  = (event) => {
        event.preventDefault();
        const password = event.target[0].value;
        const newPassword = event.target[1].value;
        const newPassword2 = event.target[2].value;
        if (newPassword !== newPassword2 || newPassword.length === 0)  {
            return;
        } else {
          const toSend = {
              type: MESSAGE_TYPE.CHANGEPASS,
              payload: {oldpass: CryptoJs.SHA256(password).toString(), password: CryptoJs.SHA256(newPassword).toString()},

          };
          socket.send(JSON.stringify(toSend));
        }
    };

    changeBackgroundVolume = (event) => {
        this.setState({bgVol: event.target.value});
        this.props.backgroundVolume(event.target.value / 100);
    };

    changeSoundVolume = (event) => {
        this.props.soundVolume(event.target.value / 100);

    };

    render() {
        return (
            <div className="Menu" id={"Menu"} >
                <button className={"close-button"} id={"settings-close"} onClick={this.props.close}>
                    <i className="fas fa-times"></i> </button>
                {this.state.showUsername?
                    <div className={"username sub-menu"}>
                        <button className={"close-button"} id={"username-close"} onClick={this.showUsername}>
                            <i className="fas fa-arrow-left"></i>
                        </button>
                        <button className={"close-button"} id={"settings-close"} onClick={this.props.close}>
                            <i className="fas fa-times"></i> </button>
                        <form className={"reset-username-form"} onSubmit={this.resetUsername}>
                            <input type={"text"} className={"input-ele"} autoComplete={"off"} placeholder={"Old Username"}/>
                            <input type={"password"} className={"input-ele"} autoComplete={"off"} placeholder={"Password"}/>
                            <input type={"text"} className={"input-ele"} autoComplete={"off"} placeholder={"New Username"}/>
                            <input type={"submit"} className={"submit-button"} value={"Confirm"}/>
                        </form>
                        <div id={"reset-username-error"}></div>
                    </div>
                    :null}

                {this.state.showPassword ?
                    <div className={"password sub-menu"}>
                        <button className={"close-button"} id={"password-close"} onClick={this.showPassword}>
                        <i className="fas fa-arrow-left"></i>
                    </button>
                    <button className={"close-button"} id={"settings-close"} onClick={this.props.close}>
                        <i className="fas fa-times"></i> </button>
                        <form className={"reset-password-form"} onSubmit={this.resetPassword}>
                        <input type={"password"} className={"input-ele"} autoComplete={"off"} placeholder={"Old Password"}/>
                        <input type={"password"} className={"input-ele"} autoComplete={"off"} placeholder={"New Password"}/>
                        <input type={"password"} className={"input-ele"} autoComplete={"off"} placeholder={"Confirm New Password"}/>
                        <input type={"submit"} className={"submit-button"} value={"Confirm"}/>
                        </form>
                        <div id={"reset-password-error"}></div>

                    </div>
                    :null
                }
                {this.state.showAudio ?
                    <div className={"audio sub-menu"}>
                        <button className={"close-button"} id={"audio-close"} onClick={this.showAudio}>
                            <i className="fas fa-arrow-left"></i>
                        </button>
                        <form>
                            Background Volume:
                            <div>
                        <i className="fas fa-volume-up"></i>
                        <input onChange={this.changeBackgroundVolume} type="range" value={this.props.bgVol} min="0" max="100"
                               className="slider" id="backgroundVolume"/>
                            </div>

                        Sound Volume:
                            <div>
                                <i className="fas fa-volume-up"></i>
                        <input onChange={this.changeSoundVolume} type="range" value={this.props.soundVol} min="0" max="100"
                               className="slider" id="backgroundVolume"/>

                            </div>
                        </form>
                    </div>
                    :null
                }
                {this.state.showMenu ? <div className={"menu"}> <h2> Settings </h2>
                    <ul className={"menu-ul"} >
                        <li onClick={this.showUsername}>Change username </li>
                        <li onClick={this.showPassword}>Change password</li>
                        <li onClick={this.showAudio}>Audio settings</li>
                        <li onClick={this.logout}>Logout</li>
                    </ul></div>:null }

            </div>
        );
    }}

export default Menu;
