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
  * Contains the filename
  *
  * @var
  * @access public
  * @type string
  */
  this._fileName = "";
  
  /**
   * Scope duplicator / parent this
   *
   * @var
   * @access private
   * @type object
   */
   var that = this;
  
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
      "file": that._fileName,
      "value": window.aceEditor.getSession().getValue()
    };
    IDE.htwg.websocket.sendMessage( msg );
  };

  //probably there might be more commands
  this.executeCommand = function(data){
    switch ( data.command ){
      case "load":
        window.aceEditor.getSession().setValue(data.text);
        this._fileName = data.filename;
        break;
      case "remove":
        this.closeTab(data.value);
        break;
      default:break;
    }
  };
  
  this.closeTab = function(files){
    jQuery.each(files, function(i, filename) {
      console.log(filename);
    });
  };

  this.init();    
};
