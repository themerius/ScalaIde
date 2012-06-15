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
    $("#theme-changer").change(this.changeTheme);
  };

  this.changeTheme = function(e){
    window.aceEditor.setTheme("ace/theme/" + $("#theme-changer").val());
  };
  
  this.handleKey = function(e) {
    
    evt = e || window.event;
    var keyCode = evt.keyCode;
    var ctrlKey = evt.ctrlKey;
    
    //do not send message for arrowkeys, home,end,pageup,pagedown
    blockedKeyCodes = [33,34,35,36,37,38,39,40];
    
    if ( jQuery.inArray(keyCode, blockedKeyCodes) > -1 ){
      return;
    }

    if ( that._fileName == "" || typeof that._fileName === "undefined" ){
      return;
    }
   
    var msg = {
      "type": "editor",
      "command": "save",
      "file": that._fileName,
      "value": window.aceEditor.getSession().getValue()
    };
    
    // If the dot was just pressed, check for auto-complete
    if ( ctrlKey && keyCode === 32 ){
      var currentPos = window.aceEditor.getCursorPosition();
      var doc = window.aceEditor.getSession().getDocument();
      {
        msg["command"] = "save-and-complete";
        msg["row"] = currentPos.row;
        msg["column"] = currentPos.column - 1;
      }
    }
    
    // If the dot was just pressed, check for auto-complete
    if ( keyCode === 190 ){
      var currentPos = window.aceEditor.getCursorPosition();
      var doc = window.aceEditor.getSession().getDocument();
      if(currentPos.column > 0 && doc.getLine(currentPos.row).charAt(currentPos.column - 1) == '.') {
        msg["command"] = "save-and-complete";
        msg["row"] = currentPos.row;
        msg["column"] = currentPos.column - 1;
      }
    }
   
    IDE.htwg.websocket.sendMessage( msg );
  };

  //probably there might be more commands
  this.executeCommand = function(data){
    switch ( data.command ){
      case "load":
        this.loadSourceFile(data);
        this.compileSourceFile(data);
        break;
      case "compile":
        console.log(data);
        this.showCompileMessage(data);
        break;
      case "complete":
        this.showCompleteOptions(data);
        break;
      case "remove":
        this.closeTab(data.value);
        break;
      default:break;
    }
  };
  
  this.loadSourceFile = function(data){
    window.aceEditor.getSession().setValue(data.text);
    
    this._fileName = data.filename;
    
    if ( this._fileName == "" || typeof this._fileName === "undefined" ){
      this._fileName = "";
      return;
    }
    $("#editorTabs").show();
    topPosition = parseInt($("#editorTabs").css("height")) + parseInt($("#editorTabs").css("top"));
    $("#editor").css("top", topPosition);
    
    openSourceFile = $("#editorTabs").find('span[title="'+ this._fileName +'"]')
    
    if ( openSourceFile.length === 0 )
    {
      shortFileName = this._fileName.substring( this._fileName.lastIndexOf("/") + 1, this._fileName.length );
      
      tab = $('<span class="tab open" title="'+ this._fileName +'">' + shortFileName + '<a href="#" class="close" title="close">&nbsp;&nbsp;&nbsp;</a></span>');
      
      tab.click( function ( event ){
        that.openTabClickHandler(this);
      });
      
      tab.find('a').click( function( event ){
        event.stopPropagation();
        that.closeTabClickHander(this);
      });
      
      $("#editorTabs").append(tab);
    }
    else{
      openSourceFile.addClass("open");
    }
  
    $("#editorTabs").find('span[title!="'+ this._fileName +'"]').each(function(i, elem){
      $(elem).removeClass("open");
    });
    
    document.title = this._fileName;
  };
  
  this.openTabClickHandler = function(elem){
    var parentTab = $(elem);

    targetSourceFile = $("#browser").find("li").filter(function () {
      var $el = $(this);
      return $el.attr("title") === parentTab.attr("title");
    });
    
    $("#browser").jstree("deselect_all"); 
    $("#browser").jstree("select_node", targetSourceFile);
  }
  
  this.closeTabClickHander = function(elem){
    var parentTab = $(elem).parent();        
    var prevTab = parentTab.prev();
    var nextTab = parentTab.next();
        
    if($(elem).parent(".open").length > 0){
      if ( nextTab.length > 0 ){
        that.loadNewTabAfterClosing(nextTab);
      }
      else if( prevTab.length > 0 ){
        that.loadNewTabAfterClosing(prevTab);
      }
      else{
        $("#browser").jstree("deselect_all");
        $("#browser").jstree("select_node", $("#root"));
        $("#editorTabs").hide();
        $("#editor").css("top", 0);
        that._fileName = "";
        window.aceEditor.getSession().setValue("Happy Coding");
      }
    }
    parentTab.remove();
  };
  
  this.loadNewTabAfterClosing = function( tab ){
    targetSourceFile = $("#browser").find("li").filter(function () {
      var $el = $(this);
      return $el.attr("title") === tab.attr("title");
    });
    
    $("#browser").jstree("deselect_all");
    $("#browser").jstree("select_node", targetSourceFile);
    
    tab.addClass("open");
  };

  this.closeTab = function(files){
    jQuery.each(files, function(i, file) {
      $("#editorTabs").find('span[title="'+ file.file +'"]').each(function(i, elem){
        that.closeTabClickHander($(elem).find("a"));
      });
    });
  };
  
  this.compileSourceFile = function(data){
    
    this._fileName = data.filename;
    
    if ( this._fileName == "" || typeof this._fileName === "undefined" ){
      this._fileName = "";
      return;
    }
    
    var msg = {
      "type": "editor",
      "command": "compile",
      "file": that._fileName
    };
    IDE.htwg.websocket.sendMessage( msg );
  };
  
  this.showCompleteOptions = function(data){    
    var currentPos = window.aceEditor.getCursorPosition();
    var doc = window.aceEditor.getSession().getDocument();
        
    if((currentPos.column - 1) == parseInt(data.column) && currentPos.row == parseInt(data.row) ) {
      IDE.htwg.completer.showCompleter(data);    
    }
  };
  
  this.showCompileMessage = function(data){
        
    IDE.htwg.error.setErrorFileIcons(data.report);
    
    if ( this._fileName != data.filename ){
      return;
    }
    
    IDE.htwg.error.setErrors(data.report);    
  };

  this.init();    
};
