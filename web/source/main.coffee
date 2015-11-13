express = require 'express'
crypto  = require 'crypto'

async   = require 'cloud/async.js'

app = express()

class Patch
  @KEY:   'key'
  @HASH:  'hash'

  @VANITY:  'vanity'
  @PHONE:   'phone'
  @CITY:    'city'
  @COUNTRY: 'country'

  @VANITY_LOWER:      Patch.VANITY  + 'LowerCase'
  @PHONE_UPDATED:     Patch.PHONE   + 'Updated'
  @LOCATION_UPDATED: 'locationUpdated'

  _vals   = {}
  _oldVals = {}

  getHash: -> _vals[Patch.HASH]
  _extractHash = (body) ->
    key = body[Patch.KEY]

    unless key?
      throw "key is required and missing"

    _vals[Patch.HASH] = makeHash key

  _extract = (body, what) ->
    val = body[what]
    return unless val

    _vals[what] = val.trim()

    switch what
      when Patch.VANITY
        _vals[Patch.VANITY_LOWER] = _vals[what].toLowerCase()

      when Patch.PHONE
        _vals[Patch.PHONE_UPDATED] = new Date()

      when Patch.CITY, Patch.COUNTRY
        _vals[Patch.LOCATION_UPDATED] = new Date()

  constructor: (body) ->
    _extractHash body

    _extract body, Patch.VANITY
    _extract body, Patch.PHONE
    _extract body, Patch.CITY
    _extract body, Patch.COUNTRY

  applyUpdates: (record, cb) ->
    record.set key, val for key, val of _vals
    record.save null, cb

  # UPDATE
  _setOldVal = (what, newVal, oldVal, cb) ->
    if oldVal is newVal
      switch what
        when Patch.PHONE
          _vals[Patch.PHONE_UPDATED] = undefined
          delete _vals[Patch.PHONE_UPDATED]

        when Patch.CITY, Patch.COUNTRY
          _vals[Patch.LOCATION_UPDATED] = undefined
          delete _vals[Patch.LOCATION_UPDATED]

      return cb null, no

    if what isnt Patch.VANITY
      _oldVals[what] = oldVal
      cb null, yes

    else
      checkVanity newVal, (err, hit) ->
        return cb err if err

        unless hit
          _oldVals[what] = oldVal
          cb null, yes

        else
          cb 'vanity taken'

  _saveAtomicUpdates = ->
    for key, val of _oldVals
      Updates = Parse.Object.extend 'Updates'
      update = new Updates()

      update.set Patch.HASH, _vals[Patch.HASH]
      update.set 'key', key
      update.set 'oldVal', _oldVals[key]
      update.set 'newVal', _vals[key]
      update.save()

  setPrevious: (record, cb) ->
    fn = (key, newVal, oldVal) -> (lcb) -> _setOldVal key, newVal, oldVal, lcb

    list = {}
    for key, val of _vals when key isnt Patch.HASH
      list[key] = fn key, val, record.get key

    async.parallel list, (err, changed) ->
      return cb err if err

      _saveAtomicUpdates()
      cb null, changed

app.use express.bodyParser()

# utility fn returning 8 first chars of SHA256 hash
makeHash = (string) ->
  crypto.createHash 'sha256'
    .update '' + string
    .digest 'hex'
    .substring 0, 8

checkVanity = (vanity, cb) ->
  vanityLower = vanity.toLowerCase()

  hashQuery = new Parse.Query 'Latest'
  hashQuery.equalTo Patch.HASH, vanityLower

  vanityQuery = new Parse.Query 'Latest'
  vanityQuery.equalTo Patch.VANITY_LOWER, vanityLower

  Parse.Query.or(hashQuery, vanityQuery).first
    success: (o) -> cb null, o
    error: (err) -> cb err

getRecord = (hash, cb) ->
  q = new Parse.Query 'Latest'
  q.equalTo 'hash', hash
  q.find
    success: (records) ->
      switch records.length
        when 0 then cb.create()
        when 1 then cb.update records[0]
        else cb.error 'too many records'

    error: cb.error

app.get '/favicon.ico', (req, res) ->
  res.send 404, 'nope.'
  return

# public API endpoint
# https://basic-data.parseapp.com/<hex>
# https://basic-data.parseapp.com/<vanity>
app.get '/:id', (req, res) ->
  id = req.params.id ? ''

  checkVanity id, (err, o) ->
    if err
      res.jsonp error: err
      return

    unless o
      res.jsonp 404, {}
      return

    res.jsonp 200,
      vanity:   o?.get 'vanity'
      hash:     o?.get 'hash'
      phone:    o?.get 'phone'
      location:
        city:     o?.get 'city'
        country:  o?.get 'country'

app.post '/update', (req, res) ->
  try
    patch = new Patch req.body

  catch error
    res.json 500, error
    return

  getRecord patch.getHash(),
    create: ->
      Latest = Parse.Object.extend 'Latest'
      record = new Latest()

      patch.applyUpdates record,
        success: -> res.json 201, 'created'
        error: ->   res.json 500, 'not created'

    update: (record) ->
      patch.setPrevious record, (err, something) ->
        if err
          res.json 500, err
          return

        patch.applyUpdates record,
          success: -> res.json 200, 'updated'
          error: ->   res.json 500, 'not updated'

    error: ->
      res.json 500, 'unknown error'

app.listen()
