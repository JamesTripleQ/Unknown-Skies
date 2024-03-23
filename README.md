## Note for Mod Authors
If you want to check whether a planet is one of the three unique types (Artificial, Magnetic, Windswept) **_DO NOT_** use `planet.getTypeId().equals("[planet id]");`, instead use `planet.getMarket().hasCondition("[planet condition id]")`. A list of condition ids can be found [here](data/campaign/market_conditions.csv).
