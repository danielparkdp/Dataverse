import React, { Component } from 'react';
import '../css/App.scss';
import '../css/BinaryTreeRepresentation.scss';

class ClickableNode extends Component {
    
    constructor(props) {
        super(props);
    }


    render(){
        return (
                <div className="node clickable-node grow" style={ { top: this.props.top+"px",left: this.props.left+"%"}} id={this.props.id} onClick={this.props.validateClick}>
                <img style= {{width: "100%"}} src={require('../img/build-tree/orange_blossom.png')}/>
                </div>)
    }
}
export default ClickableNode;