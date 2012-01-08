class TestController
  constructor: (@view, @ws_uri)->
    @connect()
    
  connect: ->
    @ws = new WebSocket(@ws_uri) 
    @view.showInfo 'connecting'
    @ws.onopen = @onSocketOpen
    @ws.onclose = @onSocketClose
    @ws.onerror = @onSocketError
    @ws.onmessage = @onMessage
  
  onSocketOpen: =>
    @view.showInfo 'connected'
    @ws.send('ping')
    
  onSocketClose: =>
    @view.showInfo 'disconnected'

  onSocketError: (errorMessage) =>
    @view.showInfo "WebSocket Error: #{errorMessage}"
    
  onMessage: (event) =>
    @view.showInfo "Server: #{event.data}"
