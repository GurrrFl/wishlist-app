from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from .config import settings
from .database import init_db
from .routes import users_router, wishlists_router, gifts_router, reservation_router

app = FastAPI(
	title= settings.app_name,
	debug= True,
	docs_url='/api/docs',
	redoc_url='/api/redoc',
)
app.add_middleware(
		CORSMiddleware,
		allow_origins= settings.cors_origins,
		allow_credentials=True,
		allow_methods=["*"],
		allow_headers=["*"]
)

app.include_router(users_router)
app.include_router(wishlists_router)
app.include_router(gifts_router)
app.include_router(reservation_router)

@app.on_event('startup')
def on_startup():
    init_db()


@app.get("/", tags=["Root"])
def root():
    return {"message": "Wishlist API is running",
		"docs": "api/docs",
        }


@app.get("/health")
def health_check():
    return {"status": "ok"}