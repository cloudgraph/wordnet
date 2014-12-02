/* additional indexes required for import related queries */
CREATE INDEX I_synsets2 ON wordnet.synsets ( pos );
CREATE INDEX I_words1 ON wordnet.words ( lemma );