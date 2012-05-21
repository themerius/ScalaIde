/**
* This script loads the project browser
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/04/23
* @license BSD license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Browser = function($){
  
  if ( jQuery('#browser').length === 0 ) {
  //no need to initialize
    return false;
  }

  /**
  * Scope duplicator / parent this
  *
  * @var
  * @access private
  * @type object
  */
  var that = this;


  this.initialize = function(){
    
    jQuery("#relPath").val("");
    jQuery("#browserMode").val("");
    jQuery("#removeFile").val("false");

    jQuery("#browser")
      .jstree({
        "plugins" : ["themes","html_data", "types", "ui", "contextmenu", "crrm"],
        "themes" : {
          "theme" : "classic"
        },
        "ui" : {
          "select_limit" : 1,
          "select_multiple_modifier" : "alt",
          "selected_parent_close" : "select_parent",
          "initially_select" : [ "root" ]
        },
        "types" : {
          "valid_children" : [ "file", "folder", "root"],
          "types" : {
            "file" : {
              "valid_children" : [ "default", "folder", "file" ], //for the create event allowing these types!
              "icon" : {
                "image" : "/assets/images/file.png"
              },
              "start_drag" : false,
              "move_node" : false,
              "remove" : false
            },
            "root" : {
              "valid_children" : [ "default", "folder", "file" ],
              "icon" : {
                "image" : "/assets/images/root.png"
              },
              "start_drag" : false,
              "move_node" : false,
              "remove" : false
            }
          }
        },
        'contextmenu' : {
          'items' : that.customMenu
        },
        "core" : {
          "animation": 0,
          "initially_open" : [ "root" ]
        }
      })

      .bind("loaded.jstree", function (event, data) {
      })
      
      .bind("rename_node.jstree", function (event, data) {

        var regex = /^[a-zA-Z._]*$/;
        if ( !regex.test(data.rslt.name) ){
          alert("Characters not allowed.");
          $("#removeFile").val("true");
          $.jstree.rollback(data.rlbk);
          return;
        }
        if ( data.rslt.obj.attr("rel") === "file" || data.rslt.obj.attr("rel") === "folder" ){

          var newFileName = jQuery("#relPath").val() + "/" + data.rslt.name;      
          
          //override newfilename for folder in rename mode
          if ( $("#browserMode").val() != "create" && data.rslt.obj.attr("rel") === "folder" ){
            var indexOfLastSlash = jQuery("#relPath").val().lastIndexOf("/");
            var newFileName = jQuery("#relPath").val().substring(0, indexOfLastSlash) + "/" + data.rslt.name; 
            alert(newFileName);
          }
          
          jQuery("#relPath").val("");
          
          var countDuplicateObjectsFiles = data.rslt.obj.siblings().filter(function () {
            var $el = $(this);
            return $el.attr("title") === newFileName && $el.attr("rel") === "file" ;
          }).length;
          
          var countDuplicateObjectsFolders = data.rslt.obj.siblings().filter(function () {
            var $el = $(this);
            return $el.attr("title") === newFileName && $el.attr("rel") === "folder" ;
          }).length;

          if ( countDuplicateObjectsFiles > 0 ){
            alert("File already exists!");
            $("#removeFile").val("true");
            $.jstree.rollback(data.rlbk);
            return;
          }

          if ( countDuplicateObjectsFolders > 0 ){
            alert("Folder already exists!");
            $("#removeFile").val("true");
            $.jstree.rollback(data.rlbk);
            return;
          }
                    
          if ( $("#browserMode").val() != "create" ){
            
            //changing all title attributes in subtree
            if ( data.rslt.obj.attr("rel") === "folder" ){
              var lengthOfOldFileName = data.rslt.obj.attr("title").length;

              data.rslt.obj.find('li').each(function(i, elem) {
                var postFix = $(elem).attr("title").substring(lengthOfOldFileName, $(elem).attr("title").length);
                $(elem).attr("title", newFileName + postFix ); 
              });
            }
            
            var msg = {
              "type": "editor",
              "command": "rename",
              "file": newFileName,
              "value": data.rslt.obj.attr("title"),
              "folder": false
            };
            
            if ( data.rslt.obj.attr("rel") === "folder" ){
              msg.folder = true;
            }
                        
            IDE.htwg.websocket.sendMessage( msg );
            
          }
          data.rslt.obj.attr("title", newFileName);
          
        }
      })
    
      .bind("delete_node.jstree", function (event, data) {
        
        var list = [];

        if ( data.rslt.obj.attr("rel") === "folder" ){
          data.rslt.obj.find('li').each(function(i, elem) {
            if ( $(elem).attr("rel") === "file" ){
              list.push({"file": $(elem).attr("title")});
            }
          });
        }
        else{
          list.push({"file": data.rslt.obj.attr("title")});
        }
        
        var msg = {
          "type": "editor",
          "command": "remove",
          "file": data.rslt.obj.attr("title"),
          "list": list
        };
                
        IDE.htwg.websocket.sendMessage( msg );
      })
      
      .bind("create_node.jstree", function (event, data) {
        $("#browserMode").val("create");
      })
      
      .bind("create.jstree", function (event, data) {

        $("#browserMode").val("");
                
        if ( $("#removeFile").val() === "true" ){
          $("#removeFile").val("false");
          $.jstree.rollback(data.rlbk);
          return;
        }
        
        var msg = {
          "type": "editor",
          "command": "create",
          "file": data.rslt.obj.attr("title"),
          "folder": false
        };
        
        if ( data.rslt.obj.attr("rel") === "folder" ){
          msg.folder = true;
        }
        
        IDE.htwg.websocket.sendMessage( msg );
      })

      .bind("select_node.jstree", function (event, data) {
        
        $("#relPath").val("");
        $("#browserMode").val("");
        
        if ( data.rslt.obj.attr("rel") === "file" ){
          var msg = {
            "type": "editor",
            "command": "load",
            "file": data.rslt.obj.attr("title")                     
           };
             
          IDE.htwg.websocket.sendMessage( msg );
        }
      })
  
  };

  this.customMenu = function(node){
    
    $("#relPath").val("");
    $("#browserMode").val("");
    $("#removeFile").val("false");
    
    var items = {
      createItem: {
        label: "Create",
        action: function (obj) {

        },
        "submenu" : {
          fileItem: {
            "label": "File",
            "action": function (obj) {
              jQuery("#relPath").val(that.getRelativePathOfObject(obj));              

              if( obj.attr("rel") === "file" ){
                position = "after";
              }
              else{
                position = "inside";
              }
                          
              $("#browser").jstree("create", obj.context.prevObject, position, { "attr": {"rel": "file"} });
            }
          },
          folderItem: {
            "label": "Folder",
            "action": function (obj) {
              jQuery("#relPath").val(that.getRelativePathOfObject(obj));              

              if( obj.attr("rel") === "file" ){
                position = "after";
              }
              else{
                position = "inside";
              }
              
              $("#browser").jstree("create", obj.context.prevObject, position, { "attr": {"rel": "folder"} });
            }
          }
        }
      },
      renameItem: {
        "label": "Rename",
        "separator_before": true,
        "action": function (obj) {
            jQuery("#relPath").val(that.getRelativePathOfObject(obj));              
            $("#browser").jstree("rename");
          }
      },
      deleteItem: {
        "label": "Delete",
        "action": function (obj) {
          if ( confirm( "Are you sure you want to delete \"" + obj.context.text.trim() + "\"?" ) ){
            $("#browser").jstree("remove");
          }
        }
      }
    }

    if ($(node).attr("rel") === "root") {
      // Delete the "delete" menu item
      delete items.deleteItem;
      delete items.renameItem;
    }

    return items;
  };

  this.getRelativePathOfObject = function(obj){

    var indexOfLastSlash = obj.attr("title").lastIndexOf("/");
        
    //we are root or folder
    if (indexOfLastSlash < 0 || obj.attr("rel") === "folder" ){
      return obj.attr("title");
    }
    
    return obj.attr("title").substring(0, indexOfLastSlash);
  };
  
  this.initialize();
};
