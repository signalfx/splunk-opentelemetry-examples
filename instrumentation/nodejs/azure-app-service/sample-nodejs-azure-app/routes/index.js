var express = require('express');
var router = express.Router();

/* GET home page. */
router.get('/', function(req, res, next) {
  req.log.info('index.js endpoint invoked, sending response');
  res.render('index', { title: 'Express' });
});

module.exports = router;
