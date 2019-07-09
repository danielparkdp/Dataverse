import React, { Component } from 'react';
import '../css/App.scss';
import '../css/BinaryTreeRepresentation.scss';
import OrangeNode from "./OrangeNode"
import ClickableNode from "./ClickableNode"

class BinaryTreeRepresentation extends Component {

    constructor(props) {
        super(props);
        this.state={
            nodes:this.props.nodes
        }
    }

    componentWillReceiveProps(nextProps) {

        this.setState({ nodes: nextProps.nodes });
      }

    makeNodesToRender = (nodes)=>{
        let nodeComponents = []
        for( let nodeID in nodes){
            let val = nodes[nodeID]
            let coords = this.idToPosition(nodeID)
            let nodeComp;
            if (val===null){
                nodeComp = <ClickableNode top={coords[0]} left = {coords[1]} validateClick = {this.props.validateClick} id={nodeID}/>
            }else{
                nodeComp = <OrangeNode top={coords[0]} left = {coords[1]} value={val} id={nodeID}/>
            }
            nodeComponents.push(nodeComp)
        }
        return nodeComponents
    }


    idToPosition(id){
        //let rootTop = 29;
        //in px
        let rootTop = 270;
        //in %
        let rootLeft = 46; //48 orig
        let heightAddition;
        let horizontalDiff=0
        let depth = id.length -1
        switch(depth){
            case 0:
                //heightAddition =0;
                heightAddition = 0;
                break;
            case 1:
               // heightAddition = 13;
               heightAddition = 60;
                break;
            case 2:
               // heightAddition = 21;
               heightAddition = 120;
                break;
            case 3:
                //heightAddition = 32;
                heightAddition = 200;
                break;
            case 4:
               // heightAddition = 42;
               heightAddition = 260;
                break;
        }
        if(id.length>1){
            if(id[1]==="l"){ //23
                horizontalDiff+=-25
            }else{
                horizontalDiff+=25
            }
        }
        if(id.length>2){ //11
            if(id[2]==="l"){
                horizontalDiff+=-12
            }else{
                horizontalDiff+=13
            }
        }
        if(id.length>3){ //6
            if(id[3]==="l"){
                horizontalDiff+=-6.5
            }else{
                horizontalDiff+=6.5
            }
        }
        if(id.length>4){ //4
            if(id[4]==="l"){
                horizontalDiff+=-3.5
            }else{
                horizontalDiff+=3.5
            }
        }

        return [parseInt(rootTop+heightAddition),parseInt(rootLeft+horizontalDiff)]
    }


    render(){
        return (<div>
                <div id="outline">
                <img style= {{height: "100%", width: "100%"}} src={require('../img/build-tree/tree_graphic.png')}/>
                </div>
                <div id="node-wrapper"> {this.makeNodesToRender(this.state.nodes)}</div>


        </div>)
    }
}
export default BinaryTreeRepresentation;
