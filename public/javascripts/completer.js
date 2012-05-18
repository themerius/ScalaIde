/**
* This script creates a completer box
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/05/18
* @license BSD common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Completer = function($){

  if ( jQuery('#editor').length == 0 ) {
      //no need to initialize
      return false;
  }
   
  /**
   * Widget window
   *
   * @var
   * @access private
   * @type object
   */
   this.win = false;
   
  /**
   * Optionlist for the widget window
   *
   * @var
   * @access private
   * @type object
   */
   this.optionList = false;
   
  /**
   * listener for the wiget window
   *
   * @var
   * @access private
   * @type object
   */
   this.listener = null;
   
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
    this.win = $('<div id="auto-complete-widget" class="widget" tabindex="0"></div>');
    this.optionList = $('<ul></ul>');
    this.win.append(this.optionList);
    
    this.win.blur(function() {
        that.close();
    });
    
    this.maxBottom = $(document).height();
    $(document.body).append(this.win);
  };

  this.showCompleter = function(data){
    
    //if completer is already opened, do not open more completer widgets
    if(this.listener) {
      return false;
    }
    
    var currentPos = window.aceEditor.getCursorPosition();
 

    console.log(data);
    console.log(currentPos);
    
    if(!data ||
       data.row != currentPos.row ||
       data.column != currentPos.column-1 ||
       data.options.length == 0
    ) {
      console.log("close");
      this.close();
      return;
    }
    
    this.showOptionList(data);
  };
  
  this.showOptionList = function(data) {
    var options = JSON.parse(data.options);
    
    this.optionList.empty();
    for(var i = 0; i < options.length; i++) {
        var option = options[i];
        var optionString = option.name + option.symType;
        var li = $('<li>' + optionString + '</li>');
        if (option.kind == "package"){
          li.addClass("package");
        }
        else if ( option.kind == "method"){
          li.addClass("method");
        }
        else if ( option.kind == "value"){
          li.addClass("value");
        }
        else if ( option.kind == "constructor"){
          li.addClass("constructor");
        }
        li.data('option', option);
        li.mouseover(function(){
          $(this).addClass("selected");
        });
        li.mouseout(function(){
          $(this).removeClass("selected");
        });
        li.click(function() {
            that.listener.chosen($(this).data('option'));
        });
        this.optionList.append(li);
    }
    
    console.log(this.optionList);
    
    var row = data.row;
    var column = data.column;
    var renderer = window.aceEditor.renderer;
    var coords = renderer.textToScreenCoordinates(row, column);
    this.win.css('left', coords.pageX);
    this.win.css('top', coords.pageY + renderer.lineHeight);
    this.win.show();
    this.win.focus();
    this.updatePosition();
    
    this.listener = new AutoCompleteListener(this, row, column);
  };
  
  this.updatePosition = function() {
    var offset = this.win.offset();
    var bottom = offset.top + this.win.height();
    if(bottom > this.maxBottom) {
        this.win.css('height', this.maxBottom - offset.top);
    } else {
        this.win.css('height', this.win.find('ul').height());
    }
  };
  
  this.close = function() {
    this.listener && this.listener.destroy();
    this.listener = null;
    
    this.win.hide();
    window.aceEditor.focus();
  };   

  function AutoCompleteListener(widget, startRow, startColumn) {
    var self = this;
    var doc = window.aceEditor.getSession().getDocument();
    
    this.init = function() {
      doc.addEventListener("change", this.onDocChange);
      widget.win.bind("keypress", this.onWidgetKeyPress);
      this.setSelectedIndex(0);
    }
    
    this.destroy = function() {
      doc.removeListener("change", this.onDocChange);
      widget.win.unbind("keypress", this.onWidgetKeyPress);
    }
    
    this.onDocChange = function(e) {
      if(e.data.range.end.row != startRow || e.data.range.end.column <= startColumn) {
        widget.close();
      }
      
      var text = self.getFilterText();
      if(text == null) {
        widget.close();
        return;
      }
      
      text = text.toLowerCase();
      widget.optionList.find('li').each(function() {
        var option = $(this).data('option');
        if(option.name.toLowerCase().indexOf(text) == 0) {
          $(this).show();
        } else {
          $(this).hide();         }
      });
      
      widget.updatePosition();
      
      self.setSelectedIndex(0);
    };
    
    this.onWidgetKeyPress = function(e) {
      switch(e.keyCode) {
        // backspace
        case 8: {
          window.aceEditor.removeLeft();
          break;
        }
        // delete
        case 46: {
          window.aceEditor.removeRight();
          break;
        }
        // enter
        case 13: {
          var li = widget.optionList.find('li:visible').eq(self.getSelectedIndex());
          self.chosen(li.data('option'));
          break;
        }
        // escape
        case 27: {
          widget.close();
          break;
        }
        // left
        case 37: {
          window.aceEditor.navigateLeft();
          break;
        }
        // up
        case 38: {
          self.setSelectedIndex(self.getSelectedIndex() - 1);
          break;
        }
        // right
        case 39: {
          window.aceEditor.navigateRight();
          break;
        }
        // down
        case 40: {
          self.setSelectedIndex(self.getSelectedIndex() + 1);
          break;
        }
        default: {
          if(e.charCode != 0) {
            window.aceEditor.insert(String.fromCharCode(e.charCode));
          }
        }
      }
        
      // If we've moved outside the range of the auto-complete text, close
      // the widget
      var currentPos = window.aceEditor.getCursorPosition();
      if(currentPos.row != startRow || currentPos.column < startColumn) {
        widget.close();
      }
      
      //console.log(e);
      //console.log(c + ': ' + e.keyCode + '/' + e.charCode);
      
      return false;
    };
    
    this.setSelectedIndex = function(index) {
      if(index < 0) {
        return;
      }
      
      var visibleElements = widget.optionList.find('li:visible');
      if(index >= visibleElements.length) {
        return;
      }
      
      widget.optionList.find('li').removeClass('selected');
      var selectedLi = visibleElements.eq(index);
      selectedLi.addClass('selected');
      var liBottom = selectedLi.offset().top + selectedLi.height();
      if(liBottom > widget.maxBottom) {
        selectedLi.get(0).scrollIntoView(false);
      } else if(liBottom < widget.win.offset().top) {
        selectedLi.get(0).scrollIntoView(true);
      }
    };
    
    this.getSelectedIndex = function() {
      return widget.optionList.find('li.selected').prevAll(':visible').length;
    };
    
    this.getFilterText = function() {
      var text = doc.getLine(startRow).substring(startColumn);
      var matches = /([a-zA-Z0-9_]+)/.exec(text);
      if(matches == null || matches.length != 2) {
          return null;
      }
      return matches[1];
    };
    
    this.chosen = function(option) {
      if(!option) {
        return;
      }
      var text = option.replaceText;
      var filterText = this.getFilterText();
      var endColumn = startColumn + (filterText ? filterText.length : 0);
      widget.close();
      doc.removeInLine(startRow, startColumn+1, endColumn);
      doc.insertInLine({row: startRow, column: startColumn+1}, text);
      window.aceEditor.moveCursorTo(startRow, startColumn + option.cursorPos);
    };
    
    this.init();
  };
  
  this.init(); 
  
};