#!/usr/bin/env ruby
require 'bundler'
Bundler.setup :default, :web

require 'sinatra'

class SocketInputWeb < Sinatra::Application
  
  helpers do 
  end
  
  # Root HTML.
  get '/' do 
    redirect '/input_test'
  end
  
  # Input test HTML.
  get '/input_test' do
    erb :input_test
  end
  
  # WebSocket input JS.
  get '/application.js' do
    coffee Dir.glob('javascripts/**/*.coffee').sort.map { |f| File.read f }.
      join("\n")
  end
  
  # WebSocket input CSS.
  get('/application.css') { 
    scss :"../stylesheets/application" }
end
