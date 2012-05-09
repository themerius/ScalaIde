/**
* This script handles terminal io
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Terminal = function($){

  this.init = function ( options ) {
    $("#terminal-input").keyup(this.handleKey);
  };

  this.handleKey = function(e) {
    var msg = {
      "type": "terminal",
      "command": "keyEvent",
      "value": e.keyCode
    };
    IDE.htwg.websocket.sendMessage(msg);
  };

  this.executeCommand = function(data){
    switch ( data.command ){
      case "response":
        $("#terminal-input").val(data.value);
        break;
      default:break;
    }
  };

  this.init();
};
