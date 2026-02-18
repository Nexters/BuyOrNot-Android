POST /api/v1/uploads/presigned-put

Request
```json
{
  "fileName": "string",
  "contentType": "string"
}
```

Response
200
```json
{
  "data": {
    "uploadUrl": "string",
    "s3ObjectKey": "string",
    "viewUrl": "string"
  },
  "message": "string",
  "status": "string",
  "errorCode": "string"
}
```

400
```json
{
  "data": null,
  "message": "string",
  "status": "string",
  "errorCode": "string"
}
```

POST /api/v1/feeds

Request
```json
{
  "category": "LUXURY" ([ LUXURY, FASHION, BEAUTY, FOOD, ELECTRONICS, TRAVEL, HEALTH, BOOK, ETC ]),
  "price": 0,
  "content": "string" (minLength: 0, maxLength: 100),
  "s3ObjectKey": "string",
  "imageWidth": 0,
  "imageHeight": 0
}
```

Response
201
```json
{
  "data": {
    "feedId": 0 (Long)
  },
  "message": "string",
  "status": "string",
  "errorCode": "string"
}
```

400
```json
{
  "data": null,
  "message": "string",
  "status": "string",
  "errorCode": "string"
}
```
