import React, { Component } from 'react';
import '../css/App.scss';
import '../css/BinaryTreeRepresentation.scss';

class OrangeNode extends Component {
    
    constructor(props) {
        super(props);
    }


    render(){
        return (
                <div className="node" style={ { top: this.props.top+"px", left: this.props.left+"%"}} id={this.props.id}>
                <div id="orange-text">{this.props.value}</div>
                <img style= {{height: "100%", width: "100%"}} src={require('../img/build-tree/orange.png')}/>
                </div>)
    }
}
export default OrangeNode;