express = require 'express'
crypto  = require 'crypto'

app = express()

# NOTE: This code is so ugly that I want to die

app.use express.bodyParser()

# utility fn returning 8 first chars of SHA256 hash
getHash = (string) ->
  crypto.createHash 'sha256'
    .update '' + string
    .digest 'hex'
    .substring 0, 8

checkVanity = (vanity, cb) ->
  vanityLower = vanity.toLowerCase()

  hashMatches = new Parse.Query 'Latest'
  hashMatches.equalTo 'hash', vanityLower

  vanityMatches = new Parse.Query 'Latest'
  vanityMatches.equalTo 'vanityLowerCase', vanityLower

  Parse.Query.or(hashMatches, vanityMatches).first
    success: (o) -> cb null, o
    error: (error) -> cb error

getRecord = (key, cb) ->
  q = new Parse.Query 'Latest'
  q.equalTo 'hash', getHash key
  q.find
    success: (records) ->
      switch records.length
        when 0 then cb.create()
        when 1 then cb.update records[0]
        else cb.error 'too many records'

    error: cb.error

# public API endpoint
# https://basic-data.parseapp.com/<hex>
# https://basic-data.parseapp.com/<vanity>
app.get '/:id', (req, res) ->
  id = req.params.id ? ''

  # some browsers ask for it and it generates errors in logs...
  if id is 'favicon.ico'
    res.end 'Really?'
    return

  checkVanity id, (err, o) ->
    if err
      res.jsonp error: err
      return

    unless o
      res.jsonp 200, {}
      return

    res.jsonp 200,
      vanity:   o?.get 'vanity'
      hash:     o?.get 'hash'
      phone:    o?.get 'phone'
      location: o?.get 'location'

resError = (res) -> (error) ->
  res.json 500, error: error

app.post '/update', (req, res) ->
  errorer = resError res

  key = req.body.key
  unless key
    return errorer ['"key" missing']

  getRecord key,
    create: ->
      Latest = Parse.Object.extend 'Latest'
      record = new Latest()

      saveable =
        hash: getHash key

      newPhone = req.body['phone']
      if newPhone
        saveable.phone = newPhone

      newLocation = req.body['location']
      if newLocation
        saveable.location = newLocation

      newVanity = req.body['vanity']
      if newVanity
        checkVanity newVanity, (err, o) ->
          unless o
            saveable.vanity = newVanity
            record.save saveable,
              success: ->
                res.json 200, action: 'created'

              error: errorer

          else res.json 500, error: 'vanity taken'

      else
        record.save saveable,
          success: ->
            res.json 200, action: 'created'

          error: errorer

    update: (record) ->
      changes = []

      # phone
      newPhone = req.body['phone']
      if newPhone
        oldPhone = record.get 'phone'
        if oldPhone isnt newPhone
          changes.push
            name: 'phone'
            oldValue: oldPhone
            newValue: newPhone

      # location
      newLocation = req.body['location']
      if newLocation
        oldLocation = record.get 'location'
        if oldLocation.city isnt newLocation.city or oldLocation.country isnt newLocation.country
          changes.push
            name: 'location'
            oldValue: oldLocation
            newValue: newLocation

      # vanity
      newVanity = req.body['vanity']
      if newVanity
        oldVanity = record.get 'vanity'
        if oldVanity isnt newVanity
          checkVanity newVanity, (err, o) ->
            unless o
              changes.push
                name: 'vanity'
                oldValue: oldVanity
                newValue: newVanity

              changes.push
                name: 'vanityLowerCase'
                oldValue: oldVanity
                newValue: newVanity.toLowerCase()

              unless changes.length
                res.json 200, action: 'not updated'
                return

              record.set change.name, change.newValue for change in changes
              record.save null,
                success: ->
                  res.json 200, action: 'updated'

                error: errorer

            else res.json 500, error: 'vanity taken'

      else
        unless changes.length
          res.json 200, action: 'not updated'
          return

        record.set change.name, change.newValue for change in changes
        record.save null,
          success: ->
            res.json 200, action: 'updated'

          error: errorer

    error: errorer

app.listen()
