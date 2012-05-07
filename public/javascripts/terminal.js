/**
* This script handles terminal io
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Terminal = function($){

  this.init = function ( options ) {
    $("#terminal-input").keypress(this.handleKey);
  };

  this.handleKey = function(e) {
    if(e.charCode == 13 || e.keyCode == 13) {  // hit return to send
      var msg = {
        "type": "terminal",
        "command": "command",
        "value": "ls"  // TODO: retrive text from terminal-input
      };
      IDE.htwg.websocket.sendMessage(msg);
    }
  };

  this.executeCommand = function(data){
    switch ( data.command ){
      case "response":
        alert(data.value);  // TODO: set received value to terminal-input
        break;
      default:break;
    }
  };

  this.init();
};
