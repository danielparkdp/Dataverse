

import React, { Component } from 'react';
import '../css/App.scss';
import '../css/Game.scss';
import '../css/Arrows.scss';
import '../css/BinaryTreeGame.scss';
import '../css/LinkedListGame.scss';
import treeLines from '../img/binary-tree/bt-prev-lines.png';

 /**
 * Visual of a binary tree of depth 4. Displays player positions in tree.
 *
 * Expects props:
 *       @props username: username of current player so it knows which node to highlight
 */
class BTPreview extends Component {

    constructor(props) {
        super(props);

        this.state = {
            playerToNode: []
        };

        this.setUpNodes();
    }

    //sets up the nodes in the binary tree structure
    setUpNodes(){
       let nRad = 12;
       let width = 280;
       let xDist = 40;
       let yDist = 40;
       let mid = width/2;
       let top = -10;


       this.infoList = [
           //row1
        {left: mid - nRad, top: top, id:0, parent:0, leftChild:1, rightChild:2},
        //row2
        {left: mid - xDist*2- nRad, top: top + yDist,                              id:1, parent:0, leftChild:3, rightChild:4},
        {left: mid + xDist*2- nRad, top: top + yDist,                              id:2, parent:0, leftChild:5, rightChild:6},
        //row3
        {left: mid - xDist*2 - xDist - nRad, top: top + yDist*2,                 id:3, parent:1, leftChild:7, rightChild:8},
        {left: mid - xDist*2- nRad + xDist, top: top + yDist*2,                  id:4, parent:1, leftChild:9, rightChild:10},
        {left: mid + xDist*2- nRad - xDist, top: top + yDist*2,                  id:5, parent:2, leftChild:11, rightChild:12},
        {left: mid + xDist*2 + xDist- nRad, top: top + yDist*2,                  id:6, parent:2, leftChild:13, rightChild:14},
         //row4
         {left: mid - xDist*2 - xDist - xDist/2 - nRad, top: top + yDist*3,       id:7, parent:3, leftChild:-1, rightChild:-1},
         {left: mid - xDist*2 - xDist + xDist/2 - nRad, top: top + yDist*3,       id:8, parent:3, leftChild:-1, rightChild:-1},
         {left: mid - xDist*2- nRad + xDist/2, top: top + yDist*3,                id:9, parent:4, leftChild:-1, rightChild:-1},
         {left: mid - xDist*2- nRad + xDist + xDist/2, top: top + yDist*3,        id:10, parent:4, leftChild:-1, rightChild:-1},
         {left: mid + xDist*2- nRad - xDist - xDist/2, top: top + yDist*3,        id:11, parent:5, leftChild:-1, rightChild:-1},
         {left: mid + xDist*2- nRad - xDist/2, top: top + yDist*3,                id:12, parent:5, leftChild:-1, rightChild:-1},
         {left:  mid + xDist*2 + xDist - xDist/2 - nRad, top: top + yDist*3,      id:13, parent:6, leftChild:-1, rightChild:-1},
         {left: mid + xDist*2 + xDist + xDist/2 - nRad, top: top + yDist*3,       id:14, parent:6, leftChild:-1, rightChild:-1},
       ];
      // nodeList.push

      this.nodeList = [];
      this.infoList.forEach((info) => {
        this.nodeList.push(<div className="prev-node" id={info.id} style={{left:info.left, top:info.top}} key={info.id}/>)
      })
    }

    //bind players of game each with a node
    bindPlayers(playerMap){
        //clear any previous players
        this.setState({playerToNode: []});

        //map each username to an active node of a certain color
        playerMap.forEach((username) => {
            this.state.playerToNode[username]  = this.infoList[0];
        });

        this.setState({}); //rerender
    }

     /* sets active node for given username and returns it- turns it a color */
    setActive(num, username){
        if(username){
            //invalid number
            if(num < 0 || num > 14){
                return;
            }
            this.state.playerToNode[username] = this.infoList[num];
            return this.infoList[num];
        }

    }

    /* sets active apple - turns it red */
    changeActive(next, username){
        let currNode = this.state.playerToNode[username];
        if(currNode){
            switch(next){
                case "left":
                    this.setActive(currNode.leftChild, username);
                    break;
                case "right":
                    this.setActive(currNode.rightChild, username)
                    break;
                case "parent":
                    this.setActive(currNode.parent, username)
                    break;
                default:
                    break;
            }
        }
    }

    render() {
        return (
        <div className="bt-preview">

            {this.nodeList}

            {/* ACTIVE PLAYER NODES */ }
            {Object.keys(this.state.playerToNode).map((user) =>
                        <div key={"active-bt-node-" + user} className={"prev-node " + (user == this.props.username ? "active-apple" : "other-user-cookie")}
                            style={{left: this.state.playerToNode[user].left + "px",
                                    top: this.state.playerToNode[user].top + "px"}}
            /> )}

        </div>
        )
    }
}

export default BTPreview;
