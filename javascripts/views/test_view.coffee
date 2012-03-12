class TestView
  constructor: ->
    @onConnect = ->
    @onDisconnect = ->
      
    @$status = $('#status')
    @$button = $('button')
    @$button.click => @onButtonClick()
    @canvas = document.getElementById 'canvas'
    @canvasHeight = @canvas.clientHeight
    @canvasWidth = @canvas.clientWidth
    
  showInfo: (message) ->
    @$status.text message
    switch message
      when 'Connected'
        @$button.html 'Disconnect'
      when 'Disconnected'
        @$button.html 'Connect'
        
  drawRect: (x, y) ->
    context = @canvas.getContext '2d'
    context.clearRect 0, 0, @canvasWidth, @canvasHeight
    context.fillStyle = '#ff0000'
    context.fillRect x, y, 10, 10
    
  onButtonClick: ->
    if @$button.html() == 'Connect'
      @onConnect()
    else
      @onDisconnect()
