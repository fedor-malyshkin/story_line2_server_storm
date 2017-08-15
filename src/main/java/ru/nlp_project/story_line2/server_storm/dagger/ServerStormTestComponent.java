package ru.nlp_project.story_line2.server_storm.dagger;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ServerStormTestModule.class})
public abstract class ServerStormTestComponent extends ServerStormComponent {

}
