import os
from typing import Optional
from pydantic import BaseSettings


class Settings(BaseSettings):
    app_name: str = "Wishlist App"
    debug: bool = True
    database_url: str = "sqlite:///./wishlist.db"
    
    cor_origins: list = ["http://localhost:5173",
                          "http://localhost:3000",
                          "http://127.0.0.1:3000",
                           "http://127.0.0.1:5173"]

static_dir: str = "static"
images_dir: str = "static/images"

class Config:
    env_file = ".env"
    
settings = Settings()