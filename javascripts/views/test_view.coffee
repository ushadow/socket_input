class TestView
  constructor: ->
    @onConnect = ->
    @onDisconnect = ->
      
    @$status = $('#status')
    @button = $('button')
    @button.click => @onButtonClick()
    
  showInfo: (message) ->
    @$status.text message
    switch message
      when 'Connected'
        @button.html 'Disconnect'
      when 'Disconnected'
        @button.html 'Connect'
    
  onButtonClick: ->
    if @button.html() == 'Connect'
      @onConnect()
    else
      @onDisconnect()
