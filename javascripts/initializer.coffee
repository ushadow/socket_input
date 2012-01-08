# Sets up everything when the document loads.
$ ->
  view = new TestView()
  controller = new TestController view, 'ws://echo.websocket.org/'
  
  # Debugging convenience.  
  window.controller = controller    