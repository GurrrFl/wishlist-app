# Android API contract

The Retrofit interfaces follow the FastAPI routes under `backend/app/routes`.
JSON field names are defined in `data/model/dto`; UI screens consume domain models.

## Routes

- Auth: `POST /users/register`, `POST /users/token`, `GET /users/me`.
- Wishlists: `GET /wishlists/my`, `GET /wishlists/{id}`, `GET /wishlists/link/{link}`, `POST /wishlists/`, `PATCH /wishlists/{id}`, `DELETE /wishlists/{id}`.
- Gifts: `GET /gifts/wishlist/{id}`, `GET /gifts/{id}`, `POST /gifts/`, `PATCH /gifts/{id}`, `DELETE /gifts/{id}`.
- Reservations: `POST /reservations/gift/{id}`, `GET /reservations/my`, `POST /reservations/{id}/cancel`.

## Integration blockers

`GET /wishlists/my` declares a list response but currently returns an object containing a `wishlists` property. The route should return the list it declares before the network source is enabled in debug or used in release.

`WishlistCreate` requires `user_id` even though the route obtains the owner from the bearer token. The client sends the session user ID to satisfy the current schema; the server should continue to derive ownership from the token.

The current `WishlistShort` response does not include gifts. The network data source must fetch details or gifts separately for screens that display them. Until this integration work is complete, the debug variant defaults to the fake source.
