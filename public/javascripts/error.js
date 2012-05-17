/**
* This script loads the editor and maps function
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/05/17
* @license BSD common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Error = function($){

  if ( jQuery('#editor').length == 0 ) {
      //no need to initialize
      return false;
  }
  
  /**
   * Contains the errors
   *
   * @var
   * @access public
   * @type string
   */
   this.rowErrorMap = {};
   
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
  };
  
  this.getCompileMessages = function(messages){
    var msgs = [];
    for ( var i = 0; i < messages.length; i++ ) {
      if ( IDE.htwg.editor._fileName === messages[i].source ){
        msgs.push({
            row: messages[i].row - 1,
            column: messages[i].column,
            text: messages[i].text,
            type: messages[i].type
        });
      }
    }
    return msgs;
  };

  this.setErrors = function(messages) {
    var errors = this.getCompileMessages(JSON.parse(messages));
    window.aceEditor.getSession().clearAnnotations();
    window.aceEditor.getSession().setAnnotations(errors);    
  };
  
  this.showErrors = function( editor ){
    
  };
  
  this.init();    
};