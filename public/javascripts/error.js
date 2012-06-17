/**
* This script shows error annotations in the ace editor
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
   * Contains all errors
   *
   * @var
   * @access public
   * @type string
   */
   this.allProblems = {};
   
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
          source: messages[i].source,
          text: messages[i].text,
          type: messages[i].type
        });
      }
    }
    return msgs;
  };
  
  this.getAllCompileMessages = function(messages){
    var msgs = [];
    for ( var i = 0; i < messages.length; i++ ) {
      msgs.push({
        row: messages[i].row - 1,
        column: messages[i].column,
        source: messages[i].source,
        text: messages[i].text,
        type: messages[i].type
      });
    }
    return msgs;
  };

  this.getErrorSourceFiles = function(messages){
    var errorFiles = [];
    for ( var i = 0; i < messages.length; i++ ) {
      errorFiles.push(messages[i].source);
    }
    
    var result = [];
    $.each(errorFiles, function(i,v){
        if ($.inArray(v, result) == -1) result.push(v);
    });
    return result;
  };
  
  this.setAllProblems = function(messages){
    try{
      this.allProblems = this.getAllCompileMessages(JSON.parse(messages));
    }catch(e){}
  };
  
  this.setErrorFileIcons = function(messages){
    var errorFiles = this.getErrorSourceFiles(JSON.parse(messages));
    $("#browser").find('li').each(function(i, elem){      
      if ( jQuery.inArray($(elem).attr("title"), errorFiles) > -1 ){
        elemError = $('<span class="errorFile"></span>');
        elemError.insertBefore($(elem).find('ins').first());
        
        $(elem).parents('ul').each( function(i, parentUl){
          elemErrorDir = $('<span class="errorFile"></span>');
          elemErrorDir.insertBefore($(parentUl).closest("li[rel='folder']").find('ins').first());
        });
        
        $("#browser").jstree("open_node", $(elem).closest("ul").closest("li"));
      }
      else{
        $(elem).parents('.errorFile').remove();
        $(elem).find('.errorFile').remove();
      }
    });
  }
  
  this.setErrors = function(messages) {
    try{
      var errors = this.getCompileMessages(JSON.parse(messages));
      window.aceEditor.getSession().clearAnnotations();
      window.aceEditor.getSession().setAnnotations(errors);
    }catch(e){}
  };
  
  this.showAllProblems = function(){
           
    $("#problems").empty();
    errorList = $("<ul></ul>");
    
    for(var i = 0; i < this.allProblems.length; i++) {
        error = this.allProblems[i];
        var li = $('<li></li>');
        var a = $('<a title="' + error.source + '">' + error.type + ': ' + error.text + ', row: ' + ( parseInt(error.row) + 1 ) + ', path: ' + error.source +'</a>' );

        a.click( function(){
          aElem = $(this);
          targetSourceFile = $("#browser").find("li").filter(function () {
            var $el = $(this);
            return $el.attr("title") === aElem.attr("title");
          });
          
          $("#browser").jstree("deselect_all");
          $("#browser").jstree("select_node", targetSourceFile);
        } );
        
        li.append(a);
        errorList.append(li);
    }
    $("#problems").append(errorList);

  };
  
  this.init();    
};