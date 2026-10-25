use winit::platform::android::activity::AndroidApp;

const ANDROID_MKSHRC: &str = r#"# Cyberterm Android shell defaults
if [ -r /system/etc/mkshrc ]; then
    . /system/etc/mkshrc
fi
if [ -r "$HOME/.mkshrc" ]; then
    . "$HOME/.mkshrc"
fi
PATH="$HOME/bin:$PATH"
export PATH
less() { command more "$@"; }
PS1='cyberterm:${| REPLY=$PWD; case $PWD in "$HOME") REPLY="~";; "$HOME"/*) REPLY="~${PWD#"$HOME"}";; esac; } $ '
"#;

#[no_mangle]
fn android_main(app: AndroidApp) {
    if let Some(home) = app.internal_data_path() {
        let bin = home.join("bin");
        let init = home.join(".cyberterm.mkshrc");
        let configured = std::fs::create_dir_all(&bin)
            .and_then(|()| std::fs::write(&init, ANDROID_MKSHRC));
        match configured {
            Ok(()) => {
                std::env::set_var("ENV", init);
                std::env::set_var("PWD", &home);
            }
            Err(error) => eprintln!("cyberterm: could not prepare Android shell profile: {error}"),
        }
    }
    cyberterm_engine::run_android(app);
}
