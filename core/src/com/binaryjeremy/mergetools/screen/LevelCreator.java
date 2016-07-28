package com.binaryjeremy.mergetools.screen;


import com.badlogic.gdx.Gdx;
        import com.badlogic.gdx.Input;
        import com.badlogic.gdx.Screen;
        import com.badlogic.gdx.graphics.GL20;
        import com.badlogic.gdx.graphics.OrthographicCamera;
        import com.twojeremys.merge.GameState;
        import com.twojeremys.merge.GameStateManager;
        import com.twojeremys.merge.Merge;
        import com.twojeremys.merge.level.Level;
import com.twojeremys.merge.screen.GameEndScreen;
import com.twojeremys.merge.screen.GameOverScreen;
import com.twojeremys.merge.screen.LevelOverScreen;
import com.twojeremys.merge.screen.MainMenuScreen;

/**
 * Created by Thisisme1 on 7/27/2016.
 */
public class LevelCreator implements Screen {
    protected final Merge game;
    protected boolean isPaused = false;
    protected GameState gs;

    private OrthographicCamera camera;
    private Level level;

    public LevelCreator(final Merge inMerge){
        this.game = inMerge;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Merge.SCREEN_WIDTH ,Merge.SCREEN_HEIGHT);

        //TODO: Debug remove
        //camera.translate(100, 100);
        //camera.zoom = 2;

        this.level = new Level(game.getNextLevel());
    }

    public LevelCreator(final Merge inMerge, GameState gs){
        this.game = inMerge;

        camera = new OrthographicCamera();
        camera.setToOrtho(false, Merge.SCREEN_WIDTH ,Merge.SCREEN_HEIGHT);

        this.level = new Level(game.getLevelById(gs.getLevelId()), gs);
        game.addPoints(gs.getTotalPoints());
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        update(delta);
        draw(delta);
    }

    private void update(float delta){
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        if (Gdx.input.isKeyPressed(Input.Keys.ESCAPE) || Gdx.input.isKeyPressed(Input.Keys.BACK)) {
            pause();        //Save the game and switch back to the main menu
            dispose();      //dispose of stuff, memory ftw
        }

        if (Gdx.input.isKeyPressed(Input.Keys.P)) {
            this.isPaused = !this.isPaused;
        }

        if (!this.isPaused) {

            this.level.update(delta);

            if (level.isGameOver()) {
                dispose();
                game.addPoints(this.level.getLevelPoints());
                game.setScreen(new GameOverScreen(game));
            }

            if (level.isPassed()) {
                game.addPoints(this.level.getLevelPoints());
                game.addExtraTime(this.level.getLevelExtraTime());
                game.addToProjectileCount(this.level.getLevelProjectilesFired());
                if (!game.hasNextLevel()) {
                    dispose();
                    game.setScreen(new GameEndScreen(game));
                } else {
                    game.setScreen(new LevelOverScreen(game, level));
                    dispose();
                }
            }

            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isTouched()) {
                level.fireProjectile();
            }
        } else {
            if (Gdx.input.isKeyPressed(Input.Keys.SPACE) || Gdx.input.isTouched()) {
                this.isPaused = false;
            }
        }
    }

    private void draw(float delta){
        Gdx.gl.glClearColor(0.1f, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        game.batch.setProjectionMatrix(camera.combined);

        if (!this.isPaused) {
            //Begin the batch to the graphics card
            game.batch.begin();

            //Draw everything in the level (Satellites, Obstacles, Projectiles, background)
            level.draw(game, delta);

            game.font.draw(game.batch, "Total Points: " + game.getTotalPoints(), 50, Merge.SCREEN_HEIGHT - 10);

            game.font.draw(game.batch, "FPS: " + Gdx.graphics.getFramesPerSecond(), 50, 20);
            game.font.draw(game.batch, "Java Heap: " + (Gdx.app.getJavaHeap() / 1048576.0f), 50, 40);
            game.font.draw(game.batch, "Native Heap: " + (Gdx.app.getNativeHeap() / 1048576.0f), 50, 60);

            game.batch.end();
        } else {
            game.batch.begin();
            game.font.draw(game.batch, "Game is Paused.  Tap to continue.", Merge.SCREEN_WIDTH/2, Merge.SCREEN_HEIGHT/2);
            game.batch.end();
        }
    }

    @Override
    public void resize(int width, int height) {
        System.out.println("GameScreen.resize() method called");
    }

    @Override
    public void pause() {
        System.out.println("GameScreen.pause() method called");
        //1. change game state to PAUSED
        this.isPaused = true;
        //2. need to create the game state
        gs = new GameState(this.game, this.level);

        //TODO 4. if play services enabled, upload game
        if (this.game.getGoogleServices().isSignedInGPGS()) {
            this.game.getGoogleServices().saveSnapshot(gs);
            System.out.println("Saving Snapshot");
        } else {//3. save to disk
            GameStateManager.saveState(gs);
        }

        game.setScreen(new MainMenuScreen(game));
    }

    @Override
    public void resume() {
        System.out.println("GameScreen.resume() method called");
        //TODO 1. if play services, download game
        //TODO 2. compare cloud to local, use newest
        //Get the cloud save (if any)

        //Get the millis from the local save
        //GameStateManager.getFileDateInMillis();

        //TODO figure out which one is newer

        //TODO 3. load save
        //   I don't think step 3 is necessary, if the game is still "running" it will open where it left off automatically.
        //4. show overlay with "tap to resume"
        this.isPaused = true;   //Start the game paused
    }

    @Override
    public void hide() {
        System.out.println("GameScreen.hide() method called");
    }

    @Override
    public void dispose() {
        //This does not get called on Desktop when the window is closed.  Instead it calls pause() and that is all.
        System.out.println("GameScreen.dispose() method called");
        level.dispose();
    }
}