
import React, { Component } from 'react';
import './css/App.scss';
import './css/IntroScreen.scss';
import {socket, MESSAGE_TYPE} from "./App";
import CryptoJs from 'crypto-js';

/**
 * Models the introduction screen for the game. Includes login and sign up buttons.
 *
 * Expects the following props:
 * @prop setCookies, a function to set browser cookies for username and password
 * @prop signUpError, a string error from signing up (optional)
 * @prop loginError, error from loggingIn (optional)
 */
class IntroScreen extends Component {

    constructor(props) {
        super(props);
        this.state = {login: false, signup: false, loginError: "", signupError: ""};
    }

    login = (event) => {
        event.preventDefault();
        const username = event.target[0].value;
        const password = event.target[1].value;
        const payload = {
            username: username,
            password: CryptoJs.SHA256(password).toString(),
        };

        this.props.setCookies(username, password);

        const toSend = {
            type: MESSAGE_TYPE.LOGIN,
            payload: payload
        };

        socket.send(JSON.stringify(toSend));
        this.setState({loginError: "", signupError: ""});

    };


    componentWillReceiveProps(nextProps, nextContext) {
        this.setState({signupError: nextProps.error, loginError: nextProps.error})

    }


    signup = (event) => {
        event.preventDefault();
        const username = event.target[0].value;
        const password = CryptoJs.SHA256(event.target[1].value).toString();
        const password_confirm = CryptoJs.SHA256(event.target[2].value).toString();

        if (password !== password_confirm || event.target[1].value.length === 0) {
            this.setState({signupError: "Invalid Password (check length and match)"})
        } else {
            this.props.setCookies(username, event.target[2].value);
            const payload = {
                username: username,
                password: password
            };

            const toSend = {
                type: MESSAGE_TYPE.SIGNUP,
                payload: payload
            };
            this.setState({loginError: "", signupError: ""});
            socket.send(JSON.stringify(toSend));
        }

    };


    continueAsGuest = () => {

        const toSend = {
            type: MESSAGE_TYPE.GUEST
        };
        socket.send(JSON.stringify(toSend));



    };

    toggleSignup = () => {

        this.setState({signup: !this.state.signup, signupError: "", loginError: ""});
    };

    toggleLogin = () => {
        this.setState({login: !this.state.login});

    };



    render() {
        const login = (
            <div id={"login"} className={"intro-popup"}>
                <h2>Login</h2>
                <button className={"close-button"} id={"login-close"} onClick={this.toggleLogin}>
                    <i className="fas fa-arrow-left"></i></button>
                <form className={"intro-form"} id={"login-form"} onSubmit={this.login}>
                    <input type={"text"}autoComplete={"off"}  id={"login-username"} placeholder={"Username"} />
                    <input type={"password"} id={"login-password"} placeholder={"Password"} />
                    <br></br>
                    <div className={"error"} id={"login-error"}>{this.state.loginError} </div>
                    <input type={"submit"} value={"Login"}/>
                </form>
        </div>);



        const signup = (
            <div id={"signup"} className={"intro-popup"} onSubmit={this.signup}>
                <h2>Sign Up</h2>
                <button className={"close-button"} id={"signup-close"} onClick={this.toggleSignup}>
                    <i className="fas fa-arrow-left"></i></button>
            <form className={"intro-form"} id={"signup-form"}>
                <div className={"input-div"}> <input type={"text"} autoComplete={"off"} id={"signup-username"} placeholder={"Username"}/> </div>
                <div className={"input-div"}> <input type={"password"} id={"signup-password"} placeholder={"Password"} /> </div>
                <div className={"input-div"}> <input type={"password"} id={"signup-password-confirm"} placeholder={"Confirm Password"}/> </div>
                <div className={"error"} id={"signup-error"}> {this.state.signupError} </div>
                <div className={"input-div"}> <input type={"submit"} value={"Signup"}/> </div>

            </form>
        </div>);



        return (
            <div className="scrollable-wrapper">
                <div className="IntroScreen">
                    <div className={"title"}> <div className="dv-logo"/>  <h1>dataverse</h1> </div>
                    {!(this.state.login || this.state.signup) ?
                        <div className={"wrapper"}>
                        <button className={"large-button intro-page-btn"} id={"login"} onClick ={this.toggleLogin}>
                        Login </button>
                        <button className={"large-button intro-page-btn"} id={"signup"} onClick={this.toggleSignup}>
                        Signup </button>
                            <div className={"guest"} onClick={this.continueAsGuest}>Continue as guest</div>

                        </div>: null
                    }
                    {this.state.login ? login: null }
                    {this.state.signup ? signup: null}

                </div>
            </div>
        );
    }
}



export default IntroScreen;
