/**
* This script loads the editor and maps function
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/04/23
* @license BSD common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Editor = function($){

  if ( jQuery('#editor').length == 0 ) {
      //no need to initialize
      return false;
  }

  /**
  * Constructor
  *
  * Initializes the editor
  *
  * @access public
  * @return void
  */
  this.init = function() {
    $("#editor").keyup(this.handleKey);
  };

  this.handleKey = function(e) {
    var msg = {
      "type": "editor",
      "command": "save",
      "file": IDE.htwg.websocket._fileName,
      "value": window.aceEditor.getSession().getValue()
    };
    IDE.htwg.websocket.sendMessage( msg );

  };

  //probably there might be more commands
  this.executeCommand = function(data){
    switch ( data.command ){
      case "load":
        window.aceEditor.getSession().setValue(data.text);
        break;
      default:break;
    }
  };

  this.init();    
};
