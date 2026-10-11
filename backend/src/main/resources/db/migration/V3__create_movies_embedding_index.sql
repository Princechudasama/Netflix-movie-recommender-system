-- Approximate nearest-neighbour index for cosine distance (<=>), used by the search query.
CREATE INDEX idx_movies_embedding_hnsw
    ON movies USING hnsw (embedding vector_cosine_ops);
