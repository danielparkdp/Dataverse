import React, { Component } from 'react';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/Arrows.scss';
import '../css/LinkedListGame.scss';

/**
 * Preview of a linked list data structure. Displays player positions in list.
 * 
 * Expects props:
 *       @props username: username of current player so it knows which node to highlight
 */
class LLPreview extends Component {
    
    constructor(props) {
        super(props);
        this.state = {
            playerToNode: []
        };
        
       
        this.setUpNodes(20);
    }

    setUpNodes(num){
       this.infoList = [];
       let nRad = 12;
       let top = -10;
       let x = 30; //start left
       let deltaX = 40;
       let deltaY = 40;
       let mid = num/2;

       for(let i = 0; i < num; i++){
           //if at the halfway point, increment top
           if(i == mid){
               top = top + deltaY;
           }

           if(i < mid){
            this.infoList.push({left: x - nRad + (i)*deltaX, top:top, id: i});
           } else {
            this.infoList.push({left: x - nRad + (num - i - 1)*deltaX, top:top, id: i});
           }
       }

      this.nodeList = [];
      this.infoList.forEach((info) => {
        this.nodeList.push(<div className="prev-node" id={info.id} style={{left:info.left, top:info.top}} key={info.id}/>)
      })
    }

    bindPlayers(playerMap){
        //clear any previous players
        this.setState({playerToNode: []});

        //map each username to an active node of a certain color
        playerMap.forEach((username) => {
            this.state.playerToNode[username]  = this.infoList[0]; 
        });

        this.setState({}); //rerender
    }

        /* sets active node and returns it- turns it a color */
    setActive(num, username){
        //invalid number
        if(num < 0 || num > this.nodeList.length - 1){
            return;
        }
        this.state.playerToNode[username] = this.infoList[num];
        return this.infoList[num];
    }

    /* sets active node to be next or prev of current*/
    changeActive(next, username){
        let currNode = this.state.playerToNode[username];
        if(currNode){
            switch(next){
                case "next":
                    let nextIndex = this.getNextIndex(currNode.id);
                    this.setActive(nextIndex, username);
                    break;
                case "prev":
                    let prevIndex = this.getPrevIndex(currNode.id);
                    this.setActive(prevIndex, username);
                    break;
                default:
                    break;
            }             
         }
    }

    getNextIndex(curr){
        if(curr == 19){
           return 0;
        } else {
           return curr + 1 % this.infoList.length;
        }
    }

    getPrevIndex(curr){
        if(curr == 0){
           return 19;
        } else {
           return curr - 1 % this.infoList.length;
        }
    }

    render() {
        return (
        <div className="ll-preview">
           {this.nodeList}
           {Object.keys(this.state.playerToNode).map((user) => 
                        <div key={"active-node-" + user} className={"prev-node active-cookie " + (user == this.props.username ? "" : "other-user-cookie")}
                            style={{left: this.state.playerToNode[user].left + "px", 
                                    top: this.state.playerToNode[user].top + "px"}} 
                        /> )}
           
            <div id="ll-prev-box"/>
        </div>
        )
    }
}

export default LLPreview;