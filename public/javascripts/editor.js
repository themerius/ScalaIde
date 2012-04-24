/**
* This script loads the editor and maps function
*
* @author sischnee <sischnee@gmail.com>
* @since 2012/04/23
* @license ace common license
*/

var IDE = IDE || {};
IDE.htwg = IDE.htwg || {};

IDE.htwg.Editor = function($){
		
    if ( jQuery('#editor').length == 0 ) {
        //no need to initialize
        return false;
    }        
    /**
    * Contains the aceSocket
    *
    * @var
    * @access public
    * @type object
    */
    this._aceSocket = false;
    
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
    this.init = function ( options ) {
    	$("#editor").keypress(this.handleKey);
    };
    
    /**
     * Contains the filename
     *
     * @var
     * @access public
     * @type string
     */
    this._fileName = "";
        
    	
    this.sendMessage = function() {
       /* aceSocket.send(JSON.stringify(
            {text: window.aceEditor.getSession().getValue()}
        )) */
    	this._aceSocket.send(window.aceEditor.getSession().getValue());
    };

    this.sendMessageSave = function() {
           /* aceSocket.send(JSON.stringify(
                {text: window.aceEditor.getSession().getValue()}
            )) */
    	this._aceSocket.send("!"+this._fileName+"!"+window.aceEditor.getSession().getValue());
    };

    this.handleKey = function(e) {
        if(e.charCode == 36 || e.keyCode == 36) {  // hit $ to send
            e.preventDefault();
            this._fileName = window.aceEditor.getSession().getValue();
            that.sendMessage();
        }
        if(e.charCode == 35 || e.keyCode == 35) {  // hit # to save file
            e.preventDefault();
            that.sendMessageSave();
        }
    };
        
    this.receiveEvent = function(event) {
        //var data = JSON.parse(event.data)

    	// Handle errors
        if(event.data.error) {
        	chatSocket.close();
            //$("#onError span").text(data.error)
            //$("#onError").show()
            alert("Error");
            return;
        } else {
        	window.aceEditor.getSession().setValue(event.data);
            //$("#onChat").show()
        }        
    };
    
    this.init();    
};
