/**
* This script serves as the websocket handler
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/05/02
* @license BSD common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Websocket = function($){
  
 /**
  * Contains the webSocket
  *
  * @var
  * @access public
  * @type object
  */
  this._websocket = false;
  
  /**
   * Contains the filename
   *
   * @var
   * @access public
   * @type string
   */
  this._fileName = "";
   
  this.sendMessage = function( msg ) {
    if ( msg.file && !msg.folder ){
      this._fileName = msg.file;
    }
    this._websocket.send( JSON.stringify( msg ) );
  };
   
   
  this.receiveEvent = function(event) {
    var data = JSON.parse(event.data);
  
    // Handle errors
    if(data.error) {
      //$("#onError span").text(data.error)
      //$("#onError").show()
      alert("Error");
      return;
    }
    else {
     
     //handle to the right class
      switch (data.type) {
        case "editor":
          IDE.htwg.editor.executeCommand( data );
          break;
        case "terminal":
          IDE.htwg.terminal.executeCommand( data );
          break;
        default:break;
      }  
    }        
  };
  
};