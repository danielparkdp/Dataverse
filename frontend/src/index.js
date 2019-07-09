import React from 'react';
import ReactDOM from 'react-dom';
import './css/index.scss';
import App from './App';
import * as serviceWorker from './serviceWorker';
import {isChrome, isFirefox, isMobile} from "react-device-detect";

 if(isMobile) {
     ReactDOM.render( <div className={"mobile-error"}>
             Sorry, you can't play Dataverse on mobile.
         </div>, document.getElementById('root'));
} else if (!isFirefox && !isChrome) {
         ReactDOM.render( <div className={"browser-unsupported-error"}>
             Sorry, this browser is not supported. Try Firefox
             or Chrome.
         </div>, document.getElementById('root'));

 } else {
     ReactDOM.render(<App />, document.getElementById('root'));

 }



// If you want your app to work offline and load faster, you can change
// unregister() to register() below. Note this comes with some pitfalls.
// Learn more about service workers: https://bit.ly/CRA-PWA
serviceWorker.unregister();
