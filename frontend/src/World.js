import React, { Component } from 'react';
import Planet from './Planet';
import applePlanet from './img/planets/apple_planet.png';
import cookiePlanet from './img/planets/cookie_planet.png';
import hamPlanet from './img/planets/hamburger_planet.png';
import candyPlanet from './img/planets/candy_planet.png';
import arenaPlanet from './img/planets/arena_planet.png';
import orangePlanet from './img/planets/orange_planet.png';
import shopPlanet from './img/planets/shop_planet_v1.png';
import LinkedListGame from './games/LinkedListGame';
import BinaryTreeGame from './games/BinaryTreeGame';
import HashMapGame from './games/HashMapGame';
import QueueGame from './games/QueueGame';
import Rocket from "./Rocket";
import rocket_purple from "./img/rockets/rocket_purple.png";
import rocket_red from "./img/rockets/rocket_red.png";
import rocket_blue from "./img/rockets/rocket_blue.png";
import rocket_gold from "./img/rockets/rocket_gold.png";
import rocket_pink from "./img/rockets/rocket_pink.png";
import rocket_green from "./img/rockets/rocket_green.png";
import rocket_fly from "./misc/rocket_engine.mp3";
import ArenaMenu from "./ArenaMenu";
import Shop from "./shop/Shop"
import Tutorial from './Tutorial';
import BuildATree from "./games/BuildATree"



let interval;

const initialState = {
    showTutorial: false,
    intersectingPlanet: false,  //whether you can press enter/space to enter a planet
    rocketDeg: 90, //initial degree of rocket
    rocketTop: 300,
    rocketLeft: 300,
    rocketSpeed: 0,
    deltaX: 0,
    deltaY: 0,
    deltaTheta: 0,
    frontStarsPos: {
        x: 0,
        y: -300,
    },

    backStarsPos: {
        x: 0,
        y: -300,
    },
    totalDelta: {
        x: 0,
        y: 0
    },
    planetsPos: {
        x: 0,
        y: 0,
    },
    upArrowDown: false,
    planets: [ <Planet top={50} left={50} id={"cookieplanet"} imgUrl={cookiePlanet} hover="Chocolate Chip Links" name={"cookie planet"}/>,
        <Planet top={550} left={300} imgUrl={applePlanet} id={"appleplanet"} hover="Bin-apple Trees" name={"apple planet"}/>,
        <Planet top={700} left={1450} imgUrl={hamPlanet} id={"hamburgerplanet"} hover={"Barbe-Queue Rush"} name={"hamburger planet"}/>,
        <Planet top={250} left={950} width={240} imgUrl={candyPlanet} id={"candyplanet"} hover={"Candy Hash Saga"} name={"candy planet"}/>,
        <Planet top={100} left={1950} width={220} imgUrl={arenaPlanet} id={"arenaplanet"} hover={"Arena [PvP]"} name={"arena planet"}/>,
        <Planet top={-450} left={600} width={220} imgUrl={shopPlanet} id={"shopplanet"} hover={"Shop"} name={"shop planet"}/>,
        <Planet top={-350} left={1400} width={220} imgUrl={orangePlanet} id={"orangeplanet"} hover={"Build A Tree"} name={"orange planet"}/>],

    planetIDs: ["cookieplanet", "appleplanet", "hamburgerplanet", "candyplanet", "arenaplanet","shopplanet","orangeplanet"],

    //all the minigames in this world
    idToGames: {},

    currGame: undefined //determines what game is being played
};


/**
 * Models the main player world, which contains planets, the user rockets
 * and games.
 *
 * Props:
 * @prop rocket - currently selected player rocket.
 * @prop username - player's username.
 * @prop coins - player's current number of coins
 * @prop speed - player's current speed?
 *
 */
class World extends Component {

    DEGREE_FACTOR = 2;
    ROCKET_ACCELERATION = .1;
    LEFT_BOUND = -1200;
    RIGHT_BOUND = 2000;
    TOP_BOUND = -1000;
    BOTTOM_BOUND = 1200;

    constructor(props) {
        super(props);

        this.currGameRef = React.createRef();

        this.state = initialState;

        this.engine = new Audio(rocket_fly);
        this.gameToPlanetMap = {
            "Choco Chip Links": "cookieplanet",
            "Bin-apple Trees": "appleplanet",
            "Candy Hash Saga": "candyplanet",
            "Arena": "arenaplanet",
            "Build A Tree":"orangeplanet"
        }
    }

    reset = () => {
        this.setState(initialState);
    };


    getRocketPos =() => {
        return this.state.totalDelta;
    };
    getOffset = (ele) =>  {
        const rect = ele.getBoundingClientRect();
        const scrollLeft = window.pageXOffset || document.documentElement.scrollLeft;
        const scrollTop = window.pageYOffset || document.documentElement.scrollTop;
        return { top: rect.top + scrollTop, left: rect.left + scrollLeft }
    };

    setVolume = (volume) => {
        this.engine.volume = volume;

    };

    updateUsername = () => {
        this.setState({idToGames: {
            "cookieplanet": <LinkedListGame onLeave={this.returnToSpace} ref={this.currGameRef} username={this.props.username} backToArena={() => this.switchToGame("Arena", false)}/>,
            "appleplanet": <BinaryTreeGame onLeave={this.returnToSpace} ref={this.currGameRef} username={this.props.username} backToArena={() => this.switchToGame("Arena", false)}/>,
            "orangeplanet": <BuildATree onLeave={this.returnToSpace} username={this.props.username} ref={this.currGameRef}/>,
            "hamburgerplanet": <QueueGame onLeave={this.returnToSpace} ref={this.currGameRef} username={this.props.username} />,
            "candyplanet": <HashMapGame onLeave={this.returnToSpace} ref={this.currGameRef} username={this.props.username} backToArena={() => this.switchToGame("Arena", false)}/>,
            "shopplanet": <Shop close={this.returnToSpace} moneyAmount = {this.props.coins}/>,
            "arenaplanet": <ArenaMenu onLeave={this.returnToSpace} username={this.props.username} ref={this.currGameRef} showGame={(name, mult) => this.switchToGame(name, mult)}/>

        }});
    };

    //switches to game that's associated with this name - returns game state
    switchToGame = (nameOfGame, multiplayer) => {
        //find planet associated with this name
        const planetDOM = this.state.idToGames[this.gameToPlanetMap[nameOfGame]];
        //if the planet wasn't found, print error
        if(planetDOM == undefined || planetDOM.ref == undefined || !planetDOM.ref.current){
            this.setState({
                currGame:  <ArenaMenu onLeave={this.returnToSpace} username={this.props.username} ref={this.currGameRef} showGame={(name, mult) => this.switchToGame(name, mult)}/> //set the current game
            });
            return this.state.currGame.ref.current;
            console.log("UNDEFINED")
        } else {
            this.setState({
                currGame: planetDOM //set the current game
            });
            planetDOM.ref.current.setState({multiplayer: multiplayer});
            return planetDOM.ref.current;
        }
    };

    enterGame = () => {
        const rocketPos = this.getOffset(document.getElementById("rocket"));
        this.state.planetIDs.forEach(ele => {
            const eleByID = document.getElementById(ele);
            if (eleByID) {
                this.updateUsername();
                const offset = this.getOffset(document.getElementById(ele));
                if (((rocketPos.top > offset.top -30) && (rocketPos.top < offset.top + 200))
                    && ((rocketPos.left > offset.left - 20) && (rocketPos.left < offset.left + 180))) {
                    this.setState({currGame: this.state.idToGames[ele]});
                }
            }
        })
    };

    /**
     * basically the same function as enterGame but instead of entering
     * a planet you just get a visual cue to press enter or space
     */
    checkIfIntersects = () => {
        let intersecting = false
        const rocketPos = this.getOffset(document.getElementById("rocket"));
        this.state.planetIDs.forEach(ele => {
            const eleByID = document.getElementById(ele);
            if (eleByID) {
                this.updateUsername();
                const offset = this.getOffset(document.getElementById(ele));
                if (((rocketPos.top > offset.top - 30) && (rocketPos.top < offset.top + 200))
                    && ((rocketPos.left > offset.left - 20) && (rocketPos.left < offset.left + 180))) {
                        intersecting = true
                }

                if (intersecting === true) {
                    this.setState({intersectingPlanet: true});
                } else {
                    this.setState({intersectingPlanet: false});
                }
            }
        });

    };
    componentDidMount() {

        document.getElementById("world-view").focus();
        interval = setInterval(() => this.updateRocketPosition(), 20);
    }

    resetRocketPos = () => {
        this.setState({
            rocketSpeed: 0,
            deltaX: 0,
            deltaY: 0,
            deltaTheta: 0,
            rocketTop: 300,
            rocketLeft: 300,
            frontStarsPos: {
                x: 0,
                y: -300,
            },

            backStarsPos: {
                x: 0,
                y: -300,
            },
            totalDelta: {
                x: 0,
                y: 0
            },
            planetsPos: {
                x: 0,
                y: 0,
            }
        });
    }


    componentWillReceiveProps(nextProps, nextContext) {
        const planets = this.state.idToGames;


        planets["arenaplanet"] = <ArenaMenu username={this.props.username}/>;
        this.setState({idToGames: planets,
            rocketSpeed: 0,
            deltaX: 0,
            deltaY: 0,
            deltaTheta: 0,
            rocketTop: 300, rocketLeft: 300, totalDelta: {x: 0, y: 0}});

        if(nextProps.showTutorial !== this.props.showTutorial) {
            this.setState({
                showTutorial: nextProps.showTutorial
            })
        }
    }

    //sends start-game response messages to current game
    onStartResp(response){

        this.engine.pause();

        if(this.state.currGame && this.currGameRef.current){
            this.currGameRef.current.onStartResp(response);
        }
    }

    //sends leave-game response messages to current game
    onLeaveResp(response){
        if(this.state.currGame && this.currGameRef.current){
           let highScore = response.payload.highScore;
           this.currGameRef.current.setState({
               highScore: highScore,
               gameOver: true
            });
        }
    }

   //sends action-game response messages to current game
    onActionResp(response){
        if(this.state.currGame && this.currGameRef.current){
            this.currGameRef.current.onActionResp(response);
        }
    }

    componentWillUnmount() {
        clearInterval(interval);
    }

    rocketToFile = (rocket) => {
        switch(rocket) {
            case("red"):
                return rocket_red;
            case("blue"):
                return rocket_blue;
            case("purple"):
                return rocket_purple;
            case("gold"):
                return rocket_gold;
            case("green"):
                return rocket_green;
            case("pink"):
                return rocket_pink;
            default:
                return rocket_red;
        }
    };

    /**
     * Sets the correct state values to start rocket movement on a
     * key down event. Handles WASD and arrow keys.
     * @param event
     */
    onKeyDown = (event) => {
        switch (event.keyCode) {
            //left, a
            case 37: case 65:
                this.setState({deltaTheta: -this.DEGREE_FACTOR});
                break;
            //right, d
            case 39: case 68:
                this.setState({deltaTheta: this.DEGREE_FACTOR});
                break;
            //up, w
            case 38: case 87:
                this.setState({upArrowDown: true});
                break;
            default:
                break;
        }
      };

    /**
     * Sets the state values to begin rocket deceleration on key up.
     * @param event
     */
    onKeyUp = (event) => {
        //nothing should happen here if a game is being played
        if(this.state.currGame){
            return;
        }

        switch( event.keyCode){
            //left, right, a, d
            case 37: case 39: case 65: case 68:
                this.setState({deltaTheta: 0});
                break;
            //up, w
            case 38: case 87:
                this.setState({upArrowDown: false, deltaX: 0, deltaY: 0});
                break;
            case 32: case 13:
                this.setState({deltaTheta: 0});
                this.setState({upArrowDown: false, deltaX: 0, deltaY: 0});
                this.enterGame();
                break;
        }
    };

      //when someone exits out of a mini game
      returnToSpace = () => {
          //reset state to world mode
            this.setState({
                currGame: undefined,
                canPlay:false,
                rocketSpeed:0,
                lastTimePlayed: Math.round((new Date()).getTime() / 1000),
                deltaTheta: 0,
                upArrowDown: false,
                deltaX: 0,
                deltaY: 0
            });

            //fix focus bug
            document.getElementById("world-view").focus();
      };

      focusOnWorld(){
        document.getElementById("world-view").focus();
      }

    /**
     * Updates the position of the rocket based on current state values.
     * If the rocket is at the borders, parallax scrolls.
      */
    updateRocketPosition = () => {
        if(this.state.currGame){
            return;
        }


        if (this.state.rocketSpeed > 2) {
          if (this.engine.volume < 0.95) {
            this.engine.volume = this.engine.volume + 0.025;
          }
        } else {
          if (this.engine.volume > 0.025) {
            this.engine.volume = this.engine.volume - 0.025;
          }
        }
        const rocketLeft = this.state.rocketLeft;
        const rocketTop = this.state.rocketTop;
        const rocketDeg = this.state.rocketDeg;

        const deltaY = -Math.cos(rocketDeg / 180 * Math.PI)*this.state.rocketSpeed;
        const deltaX = Math.sin(rocketDeg / 180 * Math.PI)*this.state.rocketSpeed;

        const totalDelta = {x: this.state.totalDelta.x + deltaX , y: this.state.totalDelta.y + deltaY};
        this.setState({totalDelta: totalDelta});

            if (((this.state.totalDelta.x >= this.RIGHT_BOUND && deltaX > 0)
                || (this.state.totalDelta.x  <= this.LEFT_BOUND && deltaX < 0))
                || (this.state.totalDelta.y  <= this.TOP_BOUND && deltaY < 0)
                || (this.state.totalDelta.y >= this.BOTTOM_BOUND && deltaY > 0)) {
                this.setState( {
                    rocketSpeed: 0
                });
                return;

        }

        //checking to see if you are hovering over a planet, will display text to let you know
        this.checkIfIntersects();

        if (this.state.upArrowDown && (this.state.rocketSpeed < (this.props.speed))) {
            let speed = this.state.rocketSpeed;
            this.setState({rocketSpeed: speed + this.ROCKET_ACCELERATION});

        } else if (!this.state.upArrowDown && (this.state.rocketSpeed > 0)) {
            let speed = this.state.rocketSpeed;
            this.setState({rocketSpeed: Math.max(speed - this.ROCKET_ACCELERATION, 0)});

        }


        if ((rocketLeft < 150 && deltaX < 0) || (rocketLeft > window.innerWidth - 400 && deltaX > 0)
            || (rocketTop < 50 && deltaY < 0) || (rocketTop > window.innerHeight - 400 && deltaY > 0)) {
                this.shift(-deltaX, -deltaY);
                this.setState({
                    rocketDeg: rocketDeg + this.state.deltaTheta,
                });
                return;
            }

        this.setState({
            rocketTop: rocketTop + deltaY,
            rocketLeft: rocketLeft + deltaX,
            rocketDeg: rocketDeg + this.state.deltaTheta,
            },);
    };


    /**
     * Shifts all the planet/stars for scrolling at borders.
     * @param deltaX
     * @param deltaY
     */
    shift = (deltaX, deltaY) => {
        const frontPos = this.state.frontStarsPos;
        const backPos = this.state.backStarsPos;
        const planetsPos = this.state.planetsPos;

        const backXFactor =  Math.floor(deltaX / 5);
        const backYFactor = Math.floor(deltaY / 5);
        this.setState({backStarsPos: {
                        x: backPos.x + backXFactor,
                        y: backPos.y + backYFactor
        },

        frontStarsPos: {
            x: frontPos.x + Math.floor(deltaX / 3),
            y: frontPos.y + Math.floor(deltaY / 3),

        },

        planetsPos: {
            x: planetsPos.x + deltaX,
            y: planetsPos.y + deltaY
        }

        });
    };



    //show the tutorial page when you enter the world if the user signs up or continues as guest
    toggleTutorial(doShow, pointerEvent){
        if(doShow === undefined){
            doShow = !this.state.showTutorial;
        }
        this.setState({
            showTutorial: doShow
        });
        if(!this.state.showTutorial){
            this.focusOnWorld();
        }
    }

    render() {

        //rocket and planet positioning
        const frontX = this.state.frontStarsPos.x.toString() + "px";
        const frontY = this.state.frontStarsPos.y.toString() + "px";
        const frontStarsStyle =  {transform: `translate(${frontX}, ${frontY} `};
        const backX = this.state.backStarsPos.x.toString() + "px";
        const backY = this.state.backStarsPos.y.toString() + "px";
        const backStarsStyle =  {transform: `translate(${backX}, ${backY} `};
        const planetsX = this.state.planetsPos.x.toString() + "px";
        const planetsY = this.state.planetsPos.y.toString() + "px";
        const planetsStyle =  {transform: `translate(${planetsX}, ${planetsY} `};

        return (
            <div>
            <div id="world-view" tabIndex="0" className="world-view" onKeyDown={this.onKeyDown} onKeyUp={this.onKeyUp} >

                <div className={"stars-back"}style={backStarsStyle}> </div>
                <div className="bg"> </div>
                <div className={"stars-front"} style={frontStarsStyle}> </div>

                 {/* SHOW TUTORIAL IF NEW USER  */}
              {this.state.showTutorial ? <Tutorial onClose={() => this.toggleTutorial(false)}/>: undefined}

                {this.state.currGame != null ?
                //if in game mode
                 this.state.currGame :

                 //if in world mode
                <div>
                    <div id="world"  className="world" onKeyDown={this.onKeyDown}>
                        <Rocket x={this.state.rocketLeft} y={this.state.rocketTop} degree={this.state.rocketDeg} img={this.rocketToFile(this.props.rocket)}/>
                        {this.state.intersectingPlanet? <div className={"canEnterPlanet"}>Press Spacebar to Enter Planet!</div> : null}
                        <div className="planet-div" style={planetsStyle}>
                            {this.state.planets} </div>
                    </div>
                </div>
             }
            </div>
            </div>
        );
    }
}


export default World;
