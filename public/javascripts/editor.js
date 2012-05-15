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
        this.loadSourceFile(data);
        this.compileSourceFile(data);
        break;
      case "compile":
        this.showCompileMessage(data);
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
      return;
    }
    $("#editorTabs").show();
    $("#editor").css("top", $("#editorTabs").css("height"));
    
    openSourceFile = $("#editorTabs").find('span[title="'+ this._fileName +'"]')
    
    if ( openSourceFile.length === 0 )
    {
      shortFileName = this._fileName.substring( this._fileName.lastIndexOf("/") + 1, this._fileName.length );
      
      tab = $('<span class="tab open" title="'+ this._fileName +'">' + shortFileName + '<a href="#" class="close">&nbsp;&nbsp;&nbsp;</a></span>');
      
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
    var closeAll = false;
        
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
      return;
    }
    
    var msg = {
      "type": "editor",
      "command": "compile",
      "file": that._fileName
    };
    IDE.htwg.websocket.sendMessage( msg );
  };
  
  this.showCompileMessage = function(data){
    console.log(data);
  };

  this.init();    
};
