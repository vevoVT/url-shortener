const express = require("express");
const request = require("request");
const app = express();
const port = 3000;
const apiBaseUrl = getBaseUrl()
const apiPath = "/url_shortener/v1/"

function getBaseUrl() {
  if (process.env.API_BASE_URL === "app") {
    return "http://app:8080"
  } else {
    return "http://localhost:8080"
  }
}

app.use(
  express.urlencoded({
    extended: true
  })
  )

app.use(express.json())

app.listen(port, () => {
  console.log(`Application started at http://localhost:${port}`);
});

app.get("/", (req, res) => {
  res.sendFile(__dirname + "/index.html");
});

app.get("/getAll",function(req,res){
  request.get(`${apiBaseUrl}${apiPath}`, { json: true }, (err, resp, body) => {
    if (err) { 
      return console.log(err); 
    }
    console.log(body);
    res.setHeader('Content-Type', 'application/json');
    res.json(body);
  });
});

app.post("/shorten", function(req,res){
  const options = {
    url: `${apiBaseUrl}${apiPath}shorten`,
    json: true,
    body: req.body
  };
  console.log(req.body);
  request.post(options, (err, resp, body) => {
    if (err) {
      return console.log(err);
    }
    console.log(body)
    res.status(201).send(body)
  });
});

app.get("/:shortCode",function(req,res){
  const shortCode = req.params.shortCode
  console.log(`Short url: ${shortCode}`);
  res.redirect(301, `http://localhost:8080${apiPath}${shortCode}`);
});