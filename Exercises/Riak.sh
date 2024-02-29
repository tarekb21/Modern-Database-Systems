#store Match information separately
curl -i -H "Content-Type: application/json" -d '{"name":"Madrid_Match", "goals": 3}' http://localhost:10011/riak/up_albouh01_Match/Madrid_Match

#retrieve Match Information:
curl http://localhost:10011/riak/up_albouh01_Match/Madrid_Match | python -mjson.tool

#store Match Information with Link to goals
curl -i -H "Content-Type: application/json" -d '{"name":"Real-Madrid", "City":"Madrid", "country":"Spain", "goals_link": "/riak/up_albouh01_Match/Madrid_Match"}' http://localhost:10011/riak/up_albouh01_Match/Match	

#retrieve Match Information:
curl http://localhost:10011/riak/up_albouh01_Match/Match | python -mjson.tool

#retrieve the Match link
curl -s http://localhost:10011/riak/up_albouh01_Match/Match | grep -o '"goals_link":\s*"[^"]*"' | cut -d '"' -f 4

#extract the Match value
curl http://localhost:10011/riak/up_albouh01_Match/Madrid_Match | python -mjson.tool

