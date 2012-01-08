class TestController
  constructor: (@view, @ws_uri)->
    @connect()
    
  connect: ->
    @ws = new WebSocket(@ws_uri) 
    @ws.onopen = => @onSocketOpen
    @ws.onclose = => @onSocketClose
    @ws.onerror = (error) => @onSocketError error
    @ws.onmessage = (event) => @onMessage event.data
  
  onSocketOpen: ->
    @view.showInfo 'connected'
    
  onSocketClose: ->
    @view.showInfo 'disconnected'

  onSocketError: (errorMessage) ->
    @view.showInfo "WebSocket Error: #{errorMessage}"
    
  onMessage: (data) ->
    @view.showInfo "Server: #{data}"
